package net.horizonsend.ion.server.command.nations.settlementZones

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
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.nations.SettlementZone
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.redis
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.i
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.gui.item
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSettlementZone
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.miscellaneous.utils.*
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.Id
import org.litote.kmongo.eq
import java.awt.Rectangle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

@CommandAlias("settlementzone|szone")
internal object SettlementZoneCommand : net.horizonsend.ion.server.command.SLCommand() {
	private const val maxHorizontalArea = 200 * 200
	private const val maxZonesPerSettlement = 150

	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(RegionSettlementZone::class.java) { c: BukkitCommandExecutionContext ->
			val arg = c.popFirstArg() ?: throw InvalidCommandArgument("Zone is required")
			return@registerContext Regions.getAllOf<RegionSettlementZone>().firstOrNull { it.name == arg }
				?: throw InvalidCommandArgument("Zone $arg not found")
		}

		registerAsyncCompletion(manager, "zones") { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val settlement = PlayerCache[player].settlementOid

			Regions.getAllOf<RegionSettlementZone>()
				.filter { settlement != null && it.settlement == settlement }
				.map { it.name }
		}
	}

	private fun getSelectionKey(sender: Player): String = "nations.settlement_zone_command.selection.${sender.uniqueId}"

	@Subcommand("pos1")
	fun onPos1(sender: Player) = onPos(sender, false)

	@Subcommand("pos2")
	fun onPos2(sender: Player) = onPos(sender, true)

	data class Selection(var first: Vec3i?, var second: Vec3i?)

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
			sender msg "&7Horizontal area:&b $horizontalArea&7, width &8(&cx&8)&7:&c $width&7, height &8(&ay&8)&7:&a $height&7, length &8(&bz&8)&7:&b $length"
			sender action "&7&oHint: Use /szone show to visualize it like this after creation"

			if (horizontalArea <= maxHorizontalArea) {
				visualizeRegion(firstPoint, secondPoint, sender, 0)
			}
		}

		redis { set("nations.settlement_zone_command.selection.${sender.uniqueId}", Gson().toJson(selection)) }
		sender msg "&aSet position ${if (second) 2 else 1} to $point"
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
				val particle = Particle.REDSTONE
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

		failIf(!SettlementZone.none(SettlementZone::name eq name)) { "A zone with that name already exists" }
	}

	private fun getZones(settlement: Id<Settlement>): List<RegionSettlementZone> {
		return Regions.getAllOf<RegionSettlementZone>().filter { it.settlement == settlement }
	}

	private fun requireSettlementWithPermission(sender: Player): Oid<Settlement> {
		val settlement: Oid<Settlement> = requireSettlementIn(sender)
		requireSettlementPermission(sender, settlement, SettlementRole.Permission.MANAGE_ZONES)
		return settlement
	}

	@Subcommand("create")
	fun onCreate(sender: Player, name: String) = asyncCommand(sender) {
		val settlement: Oid<Settlement> = requireSettlementWithPermission(sender)

		validateName(name)

		val territory: RegionTerritory = getSettlementTerritory(settlement)

		val loc = sender.location
		failIf(
			territory.world != loc.world.name || !territory.contains(
				loc.blockX,
				loc.blockY,
				loc.blockZ
			)
		) { "You're can only make zones inside of your settlement's territory" }

		val (pos1: Vec3i, pos2: Vec3i) = validateSelection(sender, settlement, territory)

		SettlementZone.create(settlement, name, pos1, pos2)

		redis { del(getSelectionKey(sender)) }

		sender msg "&aCreated settlement zone!"
		sender msg "&7To put it for sale, use '/szone set price <price> while in it." +
			" To give it a recurring rent, use '/szone set rent <rent>'." +
			" Note that rent can only be changed while it is unowned, so do that before someone buys it!" +
			" &o(If you want to set it up to be settlement-wide, just set the price to 0, buy it yourself & adjust access)"
	}

	private fun validateSelection(
		sender: Player,
		settlement: Id<Settlement>,
		territory: RegionTerritory
	): Pair<Vec3i, Vec3i> {
		val selection: Selection = getSelection(sender)
			?: fail { "You need to make a selection first! See: /szone pos1, /szone pos2" }

		val pos1 = selection.first ?: fail { "Missing pos #1! /szone pos1" }
		val pos2 = selection.second ?: fail { "Missing pos #2! /szone pos2" }

		// add one since it's inclusive of the max point
		val width = abs(pos1.x - pos2.x) + 1
		val length = abs(pos1.z - pos2.z) + 1

		val root = sqrt(maxHorizontalArea.d()).i()
		failIf(width * length > maxHorizontalArea) { "The horizontal area of a settlement zone can only be up to $maxHorizontalArea blocks ($root x $root)" }

		val x = min(pos1.x, pos2.x)
		val z = min(pos2.z, pos2.z)

		val rectangle = Rectangle(x, z, width, length)

		failIf(!territory.polygon.contains(rectangle)) { "Settlement zone must be within the territory" }

		val zones = getZones(settlement)

		failIf(zones.size >= maxZonesPerSettlement) { "Settlements can only have up to $maxZonesPerSettlement zones" }

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
		requireSettlementWithPermission(sender)

		val zones: List<RegionSettlementZone> = Regions.find(sender.location)
			.mapNotNull { it as? RegionSettlementZone }
			.onEach { zone ->
				visualizationCooldown.tryExec(sender) {
					visualizeRegion(zone.minPoint, zone.maxPoint, sender, zone.name.hashCode())
				}
			}

		failIf(zones.isEmpty()) { "No zone at your current location" }

		sender msg "&6Zone at your location:&e ${zones.joinToString { it.name }}"
	}

	private val visualizationCooldown = PerPlayerCooldown(VISUALIZATION_DURATION)

	@Subcommand("show")
	@Description("List and visualize the zone(s) in your settlement")
	fun onShow(sender: Player) = asyncCommand(sender) {
		val settlement: Oid<Settlement> = requireSettlementWithPermission(sender)

		visualizationCooldown.tryExec(sender) {
			var count = 0

			for (zone in getZones(settlement)) {
				visualizeRegion(zone.minPoint, zone.maxPoint, sender, zone.hashCode())

				count++

				val text = StringBuilder()

				text.append("&7Zone &b${zone.name}&7 from &c${zone.minPoint}&7 to &a${zone.maxPoint}&7")
				zone.owner?.let { text.append(" &8(&7Owner: ${getPlayerName(it)}&8)") }

				sender msg text.toString()
			}

			sender msg "&dListed and visualized &b$count&d zone(s)"
		}
	}

	@Subcommand("list")
	@Description("List all the zones in your settlement in a menu")
	fun onList(sender: Player) = asyncCommand(sender) {
		val settlement: Oid<Settlement> = requireSettlementWithPermission(sender)

		MenuHelper.apply {
			val items = getZones(settlement).map { zone ->
				val owner = zone.owner

				val price = zone.cachedPrice
				val rent = zone.cachedRent

				@Suppress("DEPRECATION")
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

				item.updateMeta { itemMeta -> itemMeta.setDisplayName(zone.name) }

				item.lore = listOf(
					"&7Owner:&d ${if (owner == null) "None" else getPlayerName(owner)}",
					"&7Price: ${if (price == null) "&cNot for sale" else "&e${price.toCreditsString()}"}",
					"&7Rent: ${if (rent == null) "&cNo rent" else "&a${rent.toCreditsString()}"}",
					"&7Horizontal Area:&b ${((zone.maxPoint.x - zone.minPoint.x) * (zone.maxPoint.z - zone.minPoint.z)).toText()}",
					"&7Bounds:&3 ${zone.minPoint} &8->&3 ${zone.maxPoint}",
					"Dimensions &8(&cx&7, &ay&7, &9z&8): " +
						"&c${zone.maxPoint.x - zone.minPoint.x}&7, " +
						"&a${zone.maxPoint.y - zone.minPoint.y}&7, " +
						"&9${zone.maxPoint.z - zone.minPoint.z}"
				).map { it.colorize() }

				return@map guiButton(item)
			}

			Tasks.sync {
				sender.openPaginatedMenu("${getSettlementName(settlement)} Zones", items)
			}
		}
	}

	/** This should theoretically be handled by the context resolver, but.. just in case ;)*/
	private fun requireSettlementWithPermissionAndZone(sender: Player, zone: RegionSettlementZone): Oid<Settlement> {
		val settlement = requireSettlementWithPermission(sender)
		failIf(settlement != zone.settlement) { "Zone ${zone.name} isn't in your settlement!" }
		return settlement
	}

	@Subcommand("delete|remove")
	@Description("Delete the specified zone")
	@CommandCompletion("@zones")
	fun onDelete(sender: Player, zone: RegionSettlementZone) = asyncCommand(sender) {
		requireSettlementWithPermissionAndZone(sender, zone)
		val owner: SLPlayerId? = zone.owner

		SettlementZone.delete(zone.id)

		sender msg "&aDeleted settlement zone ${zone.name}"

		if (owner != null) {
			Notify.player(
				owner.uuid,
				MiniMessage.miniMessage().deserialize("Your settlement zone ${zone.name} was deleted by ${sender.name}")
			)
		}
	}

	private fun requireUnowned(zone: RegionSettlementZone) {
		zone.owner?.let { fail { "You can't do that with owned zones; ${zone.name} is owned by ${getPlayerName(it)}" } }
	}

	private fun getZoneData(zone: RegionSettlementZone): SettlementZone = SettlementZone.findById(zone.id)
		?: error("Zone ${zone.name} only exists in cache!")

	@Subcommand("set price")
	@Description("Set the price of the specified zone. Must be unowned. Use -1 for not for sale")
	@CommandCompletion("@zones -1|0|1000")
	fun onSetPrice(sender: Player, zone: RegionSettlementZone, price: Int) = asyncCommand(sender) {
		requireSettlementWithPermissionAndZone(sender, zone)
		requireUnowned(zone)

		failIf(price < -1) { "Price must not be less than 0. To make it unavailable, use -1. To make it free, make it 0." }

		val newPrice = if (price == -1) null else price
		val oldPrice = getZoneData(zone).price
		failIf(oldPrice == newPrice) { "Price is already set to ${newPrice?.toCreditsString() ?: "none"}" }

		SettlementZone.setPrice(zone.id, newPrice)
		sender msg when {
			newPrice == null -> "&aMade ${zone.name} not for sale"
			oldPrice == null -> "&aPut zone ${zone.name} up for sale for ${newPrice.toCreditsString()}." +
				" It can now be purchased using /splot buy while standing in it." +
				" (To make it no longer for sale, use /szone set price ${zone.name} -1)"

			else -> "&aChanged price of ${zone.name} from ${oldPrice.toCreditsString()} to ${newPrice.toCreditsString()}"
		}
	}

	@Subcommand("set rent")
	@Description("Set the hourly rent of the specified zone. Must be unowned. Use 0 for no rent")
	@CommandCompletion("@zones 0|1|2|3|4|5")
	fun onSetRent(sender: Player, zone: RegionSettlementZone, rent: Int) = asyncCommand(sender) {
		requireSettlementWithPermissionAndZone(sender, zone)
		requireUnowned(zone)

		failIf(rent < 0) { "Rent must not be less than 1. To make it have no rent, use 0." }

		val newRent = if (rent == 0) null else rent
		val oldRent = getZoneData(zone).rent
		failIf(oldRent == newRent) { "Rent is already set to ${newRent?.toCreditsString() ?: "none"}" }

		SettlementZone.setRent(zone.id, newRent)
		sender msg when {
			newRent == null -> "&aMade zone ${zone.name} have no rent"
			oldRent == null -> "&aGave zone ${zone.name} a rent of ${newRent.toCreditsString()} a day"
			else -> "&aChanged rent of ${zone.name} from ${oldRent.toCreditsString()} to ${newRent.toCreditsString()}"
		}
	}

	@Subcommand("reclaim")
	@Description("Reclaim the zone and")
	fun onReclaim(sender: Player, zone: RegionSettlementZone) = asyncCommand(sender) {
		val settlement = requireSettlementWithPermissionAndZone(sender, zone)
		val owner = zone.owner ?: fail { "Zone ${zone.name} is not owned" }

		SettlementZone.setOwner(zone.id, null)

		sender msg "&aReclaimed region ${zone.name} from ${getPlayerName(owner)}"

		val message = MiniMessage.miniMessage()
			.deserialize("<gray>${sender.name} reclaimed your plot ${zone.name} in ${getSettlementName(settlement)}")
		Notify.player(owner.uuid, message)
	}
}
