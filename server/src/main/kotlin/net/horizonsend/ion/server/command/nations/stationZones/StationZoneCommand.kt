package net.horizonsend.ion.server.command.nations.stationZones

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.google.gson.Gson
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.StationZone
import net.horizonsend.ion.common.database.schema.nations.spacestation.SpaceStationInterface
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.configuration.redis
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.i
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.horizonsend.ion.common.utils.text.isAlphanumeric
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.nations.SpaceStationCommand
import net.horizonsend.ion.server.features.nations.gui.item
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSpaceStation
import net.horizonsend.ion.server.features.nations.region.types.RegionStationZone
import net.horizonsend.ion.server.features.space.spacestations.CachedSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
import net.horizonsend.ion.server.gui.invui.misc.util.input.ItemMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.litote.kmongo.eq
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

@CommandAlias("stationzone|stzone")
object StationZoneCommand : SLCommand() {
    private const val maxHorizontalArea = 200 * 200
    private const val maxZonesPerStation = 150

    override fun onEnable(manager: PaperCommandManager) {
        manager.commandContexts.registerContext(RegionStationZone::class.java) { c: BukkitCommandExecutionContext ->
            val arg = c.popFirstArg() ?: throw InvalidCommandArgument("Zone is required")
            return@registerContext Regions.getAllOf<RegionStationZone>().firstOrNull() { it.name == arg }
                ?: throw InvalidCommandArgument("Zone $arg not found")
        }

        registerAsyncCompletion(manager, "stationZones") { c ->
            val player = c.player ?: throw InvalidCommandArgument("Players only")
            val stations = SpaceStationCache.all().filter { station -> station.hasOwnershipContext(player.slPlayerId) }

            Regions.getAllOf<RegionStationZone>()
                .filter { zone -> stations.any { station -> station.databaseId == zone.station } }
                .map { it.name }
        }
    }

    private fun getSelectionKey(sender: Player): String = "nations.station_zone_command.selection.${sender.uniqueId}"

    data class Selection(var first: Vec3i?, var second: Vec3i?)

    @Subcommand("pos1")
    fun onPos1(sender: Player) = onPos(sender, false)

    @Subcommand("pos2")
    fun onPos2(sender: Player) = onPos(sender, true)

    private fun getSelection(sender: Player): Selection? {
        return redis {
            get(getSelectionKey(sender))
        }?.let { json ->
            Gson().fromJson(json, Selection::class.java)
        }
    }

    private fun onPos(sender: Player, second: Boolean) = asyncCommand(sender) {
        val selection: Selection = getSelection(sender) ?: Selection(null, null)

        val point = Vec3i(sender.location)

        when {
            second -> selection.second = point
            else -> selection.first = point
        }

        val firstPoint = selection.first
        val secondPoint = selection.second

        if (firstPoint != null && secondPoint != null) {
            // add one as it is inclusive of the max point
            val width = abs(firstPoint.x - secondPoint.x) + 1
            val height = abs(firstPoint.y - secondPoint.y) + 1
            val length = abs(firstPoint.z - secondPoint.z) + 1

            val horizontalArea = width * length
            sender.information("Horizontal area: $horizontalArea, width (x): $width, height (y): $height, length (z): $length")
            sender.informationAction("Hint: Use /stzone show to visualize it like this after creation")

            if (horizontalArea <= maxHorizontalArea) {
                visualizeRegion(firstPoint, secondPoint, sender, 0)
            }
        }

        redis { set("nations.station_zone_command.selection.${sender.uniqueId}", Gson().toJson(selection)) }
        sender.success("Set position ${if (second) 2 else 1} to $point")
    }

    const val VISUALIZATION_DURATION = 4000L

    fun visualizeRegion(firstPoint: Vec3i, secondPoint: Vec3i, sender: Player, seed: Int) {
        val points = getHollowCube(firstPoint, secondPoint)

        val random = Random(seed.toLong())

        val red = random.nextInt(100, 120)
        val green = random.nextInt(100, 255)
        val blue = random.nextInt(100, 255)

        val start = System.currentTimeMillis()

        Tasks.bukkitRunnable {
            if (System.currentTimeMillis() - start > VISUALIZATION_DURATION) {
                cancel()
            }

            for ((x, y, z) in points) {
                val particle = Particle.DUST
                val color = Color.fromRGB(red, green, blue)
                val dustOptions = Particle.DustOptions(color, 100f)
                val count = 0
                sender.world.spawnParticle(particle, x.d(), y.d(), z.d(), count, dustOptions)
            }
        }.runTaskTimerAsynchronously(IonServer, 2, 2)
    }

    // https://www.spigotmc.org/threads/create-particles-in-cube-outline-shape.65991/
    private fun getHollowCube(corner1: Vec3i, corner2: Vec3i): List<Vec3i> {
        val result = mutableListOf<Vec3i>()
        val minX = min(corner1.x, corner2.x)
        val minY = min(corner1.y, corner2.y)
        val minZ = min(corner1.z, corner2.z)
        // add one as the full second block top corner is included
        val maxX = max(corner1.x, corner2.x) + 1
        val maxY = max(corner1.y, corner2.y) + 1
        val maxZ = max(corner1.z, corner2.z) + 1

        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    var components = 0
                    if (x == minX || x == maxX) components++
                    if (y == minY || y == maxY) components++
                    if (z == minZ || z == maxZ) components++
                    if (components >= 2) {
                        result.add(Vec3i(x, y, z))
                    } else {
                        val relX = x - minX
                        val relY = y - minY
                        val relZ = z - minZ

                        if (relX > 0 && relY > 0 && relZ > 0 && relX % 3 == 0 && relY % 3 == 0 && relZ % 3 == 0) {
                            result.add(Vec3i(x, y, z))
                        }
                    }
                }
            }
        }

        return result
    }

    private fun validateName(name: String) {
        failIf(name != name.lowercase(Locale.getDefault())) { "Name must be lowercase" }

        failIf(name.length !in 3..50) { "Name must be between 5 and 50 characters" }

        failIf(!name.replace("_", "").isAlphanumeric()) { "Name must use numbers, letters, and underscored only" }

        failIf(name.startsWith("_") || name.endsWith("_")) { "Name cannot start or end with underscores" }

        failIf(!StationZone.none(StationZone::name eq name)) { "A zone with that name already exists" }
    }

    fun getZones(station: CachedSpaceStation<*, *, *>): List<RegionStationZone> {
        @Suppress("UNCHECKED_CAST")
        return Regions.getAllOf<RegionStationZone>().filter { it.station == (station.databaseId as Oid<SpaceStationInterface<*>>) }
    }

    @Subcommand("create")
    fun onCreate(sender: Player, name: String, station: CachedSpaceStation<*, *, *>) = asyncCommand(sender) {
        SpaceStationCommand.requireStationOwnership(sender.slPlayerId, station)

        validateName(name)

        val loc = sender.location
        failIf(loc.world.name != station.world || !station.contains(loc.x.toInt(), loc.z.toInt())) {
            "You can only make zones inside of your station"
        }

        val (pos1: Vec3i, pos2: Vec3i) = validateSelection(sender, station)

        @Suppress("UNCHECKED_CAST")
        StationZone.create(station.databaseId as Oid<SpaceStationInterface<*>>, station.world, name, pos1, pos2)

        redis { del(getSelectionKey(sender)) }

        sender.success("Created station zone!")
        sender.success("To put it for sale, use '/stzone set price <price> while in it." +
                " To give it a recurring rent, use '/stzone set rent <rent>'." +
                " Note that rent can only be changed while it is unowned, so do that before someone buys it!" +
                " (If you want to set it up to be settlement-wide, just set the price to 0, buy it yourself & adjust access)"
        )
    }

    private fun validateSelection(sender: Player, station: CachedSpaceStation<*, *, *>): Pair<Vec3i, Vec3i> {
        val selection: Selection = getSelection(sender)
            ?: fail { "You need to make a selection first! See: /stzone pos1, /stzone pos2" }

        val pos1 = selection.first ?: fail { "Missing pos #1! /stzone pos1" }
        val pos2 = selection.second ?: fail { "Missing pos #2! /stzone pos2" }

        // add one since it's inclusive of the max point
        val width = abs(pos1.x - pos2.x) + 1
        val length = abs(pos1.z - pos2.z) + 1

        val root = sqrt(maxHorizontalArea.d()).i()
        failIf(width * length > maxHorizontalArea) { "The horizontal area of a settlement zone can only be up to $maxHorizontalArea blocks ($root x $root)" }

        val stationCenter = Vector(station.x, 192, station.z)

        failIf(stationCenter.distanceSquared(Vector(pos1.x, 192, pos1.z)) > station.radius.squared() ||
                stationCenter.distanceSquared(Vector(pos1.x, 192, pos2.z)) > station.radius.squared() ||
                stationCenter.distanceSquared(Vector(pos2.x, 192, pos2.z)) > station.radius.squared() ||
                stationCenter.distanceSquared(Vector(pos2.x, 192, pos1.z)) > station.radius.squared()
        ) {
            "Station zone must be within the station"
        }

        val zones = getZones(station)

        failIf(zones.size >= maxZonesPerStation) { "Stations can only have up to $maxZonesPerStation zones" }

        val minPoint = Vec3i(min(pos1.x, pos2.x), min(pos1.y, pos2.y), min(pos1.z, pos2.z))
        val maxPoint = Vec3i(max(pos1.x, pos2.x), max(pos1.y, pos2.y), max(pos1.z, pos2.z))

        for (zone in zones) {
            failIf(
                boolean = (zone.minPoint.x <= maxPoint.x && zone.maxPoint.x >= minPoint.x) &&
                        (zone.minPoint.y <= maxPoint.y && zone.maxPoint.y >= minPoint.y) &&
                        (zone.minPoint.z <= maxPoint.z && zone.maxPoint.z >= minPoint.z)
            ) { "That selection overlaps with ${zone.name}!" }
        }

        return minPoint to maxPoint
    }

    @Subcommand("here")
    @Description("Show zone you're standing in")
    fun onHere(sender: Player) = asyncCommand(sender) {
        val station = Regions.findFirstOf<RegionSpaceStation<*, *>>(sender.location) ?: fail { "You are not standing in a station" }

        SpaceStationCommand.requireStationOwnership(sender.slPlayerId, SpaceStationCache[station.name]!!) // assert is probably ok here since the fail above should catch

        val zones = getZones(SpaceStationCache[station.name]!!).filter { zone ->
            zone.contains(sender.location)
        }
        failIf(zones.isEmpty()) { "No zone at your current location" }

        sender.information("Zone at your location: ${zones.joinToString { it.name }}")
    }

    private var visualizationCooldown = PerPlayerCooldown(VISUALIZATION_DURATION)

    @Subcommand("show")
    @Description("List and visualize the zone(s) in your station")
    fun onShow(sender: Player) = asyncCommand(sender) {
        val station = Regions.findFirstOf<RegionSpaceStation<*, *>>(sender.location) ?: fail { "You are not standing in a station" }
        SpaceStationCommand.requireStationOwnership(sender.slPlayerId, SpaceStationCache[station.name]!!) // assert is probably ok here since the fail above should catch

        visualizationCooldown.tryExec(sender) {
            var count = 0

            for (zone in getZones(SpaceStationCache[station.name]!!)) {
                visualizeRegion(zone.minPoint, zone.maxPoint, sender, zone.name.hashCode())

                count++

                val owner = zone.owner
                val text = ofChildren(
                    Component.text("Zone ", NamedTextColor.GRAY),
                    Component.text("${zone.name} ", NamedTextColor.AQUA),
                    Component.text("from ", NamedTextColor.GRAY),
                    Component.text("${zone.minPoint} ", NamedTextColor.RED),
                    Component.text("to ", NamedTextColor.GRAY),
                    Component.text("${zone.maxPoint} ", NamedTextColor.GREEN),

                    if (owner != null) ofChildren(
                        Component.text("(", NamedTextColor.DARK_GRAY),
                        Component.text("Owner: ${getPlayerName(owner)}", NamedTextColor.GRAY),
                        Component.text(")", NamedTextColor.DARK_GRAY),
                    ) else Component.empty()
                )

                sender.sendMessage(text)
            }

            sender.success("Listed and visualized $count zone(s)")
        }
    }

    @Subcommand("list")
    @Description("List all the zones in your settlement in a menu")
    fun onList(sender: Player, station: CachedSpaceStation<*, *, *>) = asyncCommand(sender) {
        SpaceStationCommand.requireStationOwnership(sender.slPlayerId, station)
        val items = getZones(station).map { zone ->
            val owner = zone.owner

            val price = zone.cachedPrice
            val rent = zone.cachedRent

            val item: ItemStack = when {
                owner != null -> {
                    skullItem(owner.uuid, getPlayerName(owner))
                }

                price != null && rent != null -> {
                    item(Material.GREEN_WOOL)
                }
                price != null -> {
                    item(Material.LIME_WOOL)
                }

                rent != null -> {
                    item(Material.RED_WOOL)
                }

                else -> item(Material.COBWEB)
            }

            item.updateDisplayName(Component.text(zone.name))
                .updateLore(listOf(
                    ofChildren(
                        Component.text("Owner: ", NamedTextColor.GRAY),
                        if (owner == null) Component.text("None")
                        else Component.text(getPlayerName(owner), NamedTextColor.LIGHT_PURPLE)
                    ),
                    ofChildren(
                        Component.text("Price: ", NamedTextColor.GRAY),
                        if (price == null) Component.text("Not for sale", NamedTextColor.RED)
                        else Component.text(price.toCreditsString(), NamedTextColor.YELLOW)
                    ),
                    ofChildren(
                        Component.text("Rent: ", NamedTextColor.GRAY),
                        if (rent == null) Component.text("Not for sale", NamedTextColor.RED)
                        else Component.text(rent.toCreditsString(), NamedTextColor.GREEN)
                    ),
                    ofChildren(
                        Component.text("Horizontal Area: ", NamedTextColor.GRAY),
                        Component.text(((zone.maxPoint.x - zone.minPoint.x) * (zone.maxPoint.z - zone.minPoint.z)).toText(), NamedTextColor.AQUA)
                    ),
                    ofChildren(
                        Component.text("Bounds: ", NamedTextColor.GRAY),
                        Component.text("${zone.minPoint}", NamedTextColor.DARK_AQUA),
                        Component.text("->", NamedTextColor.DARK_GRAY),
                        Component.text("${zone.maxPoint}", NamedTextColor.DARK_AQUA)
                    ),
                    ofChildren(
                        Component.text("Dimensions ", NamedTextColor.WHITE),
                        Component.text("(", NamedTextColor.DARK_GRAY),
                        Component.text("x", NamedTextColor.RED),
                        Component.text(", ", NamedTextColor.GRAY),
                        Component.text("y", NamedTextColor.GREEN),
                        Component.text(", ", NamedTextColor.GRAY),
                        Component.text("z", NamedTextColor.BLUE),
                        Component.text("): ", NamedTextColor.DARK_GRAY),
                        Component.text("${zone.maxPoint.x - zone.minPoint.x}", NamedTextColor.RED),
                        Component.text(", ", NamedTextColor.GRAY),
                        Component.text("${zone.maxPoint.y - zone.minPoint.y}", NamedTextColor.GREEN),
                        Component.text(", ", NamedTextColor.GRAY),
                        Component.text("${zone.maxPoint.z - zone.minPoint.z}", NamedTextColor.BLUE),
                    )
                )).makeGuiButton { _, _ -> }
        }

        ItemMenu(
            title = Component.text("${station.name} Zones"),
            viewer = sender,
            guiItems = items,
            backButtonHandler = { sender.closeInventory() }
        ).openGui()
    }

    private fun requireStationWithPermissionAndZone(sender: Player, zone: RegionStationZone): Oid<SpaceStationInterface<*>>  {
        val stationId = zone.station
        val stations = SpaceStationCommand.getOwnedStationList(sender, Any::class)
        @Suppress("UNCHECKED_CAST")
        val station = stations.firstOrNull { station -> (station.databaseId as Oid<SpaceStationInterface<*>>) == stationId }
        failIf(station == null) { "You do not own this station" }
        @Suppress("UNCHECKED_CAST")
        return (station!!.databaseId as Oid<SpaceStationInterface<*>>) // not null assertion is fine here as failIf should catch errors
    }

    @Subcommand("delete|remove")
    @Description("Delete the specified zone")
    @CommandCompletion("@stationZones")
    fun onDelete(sender: Player, zone: RegionStationZone) = asyncCommand(sender) {
        requireStationWithPermissionAndZone(sender, zone)
        val owner: SLPlayerId? = zone.owner

        StationZone.delete(zone.id)

        sender.success("Deleted station zone ${zone.name}")

        if (owner != null) {
            Notify.playerCrossServer(
                owner.uuid,
                miniMessage.deserialize("Your settlement zone ${zone.name} was deleted by ${sender.name}")
            )
        }
    }

    private fun requireUnowned(zone: RegionStationZone) {
        zone.owner?.let { fail { "You can't do that with owned zones; ${zone.name} is owned by ${getPlayerName(it)}" } }
    }

    private fun getZoneData(zone: RegionStationZone): StationZone = StationZone.findById(zone.id) ?: error("Zone ${zone.name} only exists in cache!")

    @Subcommand("set price")
    @Description("Set the price of the specified zone. Must be unowned. Use -1 for not for sale")
    @CommandCompletion("@stationZones -1|0|1000")
    fun onSetPrice(sender: Player, zone: RegionStationZone, price: Int) = asyncCommand(sender) {
        requireStationWithPermissionAndZone(sender, zone)
        requireUnowned(zone)

        failIf(price < -1) { "Price must not be less than 0. To make it unavailable, use -1. TO make it free, make it 0." }

        val newPrice = if (price == -1) null else price

        val oldPrice = getZoneData(zone).price
        failIf(oldPrice == newPrice) { "Price is already set to ${newPrice?.toCreditsString() ?: "none"}" }

        StationZone.setPrice(zone.id, newPrice)
        if (newPrice == null) sender.success("Made ${zone.name} not for sale")
        else if (oldPrice == null) sender.success("Put zone ${zone.name} up for sale for ${newPrice.toCreditsString()}. " +
                "It can now be purchased using /stplot buy while standing in it. " +
                "(To make it no longer for sale, use /stzone set price ${zone.name} -1)")
        else sender.success("Changed price of ${zone.name} from ${oldPrice.toCreditsString()} to ${newPrice.toCreditsString()}")
    }

    @Subcommand("set rent")
    @Description("Set the hourly rent of the specified zone. Must be unowned. Use 0 for no rent")
    @CommandCompletion("@stationZones 0|1|2|3|4|5")
    fun onSetRent(sender: Player, zone: RegionStationZone, rent: Int) = asyncCommand(sender) {
        requireStationWithPermissionAndZone(sender, zone)
        requireUnowned(zone)

        failIf(rent < 0) { "Rent must not be less than 1. To make it have no rent, use 0." }

        val newRent = if (rent == 0) null else rent
        val oldRent = getZoneData(zone).rent

        failIf(oldRent == newRent) { "Rent is already set to ${newRent?.toCreditsString() ?: "none"}" }

        StationZone.setRent(zone.id, newRent)
        if (newRent == null) sender.success("Made zone ${zone.name} have no rent")
        else if (oldRent == null) sender.success("Gave zone ${zone.name} a rent of ${newRent.toCreditsString()}")
        else sender.success("Changed rent of ${zone.name} from ${oldRent.toCreditsString()} to ${newRent.toCreditsString()}")
    }

    @Subcommand("reclaim")
    @Description("Reclaim a station zone and set the owner to null")
    @CommandCompletion("@stationZones")
    fun onReclaim(sender: Player, zone: RegionStationZone) = asyncCommand(sender) {
        val station = requireStationWithPermissionAndZone(sender, zone)
        val owner = zone.owner ?: fail { "Zone ${zone.name} is not owned" }

        StationZone.setOwner(zone.id, null)

        sender.success("Reclaimed region ${zone.name} from ${getPlayerName(owner)}")

        val message = MiniMessage.miniMessage()
            .deserialize("<gray>${sender.name} reclaimed your plot ${zone.name} in ${SpaceStationCache[station]?.name}")
        Notify.playerCrossServer(owner.uuid, message)
    }
}