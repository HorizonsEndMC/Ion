package net.starlegacy.command.misc

import com.sk89q.worldedit.EmptyClipboardException
import com.sk89q.worldedit.LocalSession
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.session.ClipboardHolder
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.misc.Shuttle
import net.starlegacy.feature.misc.Shuttles
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.starlegacy.util.*
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

@CommandAlias("shuttle")
@CommandPermission("starlegacy.shuttle.admin")
object ShuttleCommand : SLCommand() {
    private val worldEditPlugin: WorldEditPlugin get() = JavaPlugin.getPlugin(WorldEditPlugin::class.java)

    private fun validateName(name: String) {
        failIf(name != name.toLowerCase()) { "Name must be lowercase" }
        failIf(!name.replace("_", "").isAlphanumeric()) { "Name must use only letters, numbers, and underscores" }
    }

    @Subcommand("schem save")
    fun onSchemSave(sender: Player, name: String) {
        validateName(name)

        val session: LocalSession = worldEditPlugin.getSession(sender)
        val holder: ClipboardHolder = try {
            session.clipboard
        } catch (e: EmptyClipboardException) {
            fail { "You don't have anything in your clipboard!" }
        }
        val clipboard: Clipboard = holder.clipboard

        val file = Shuttles.getSchematicFile(name)
        writeSchematic(clipboard, file)

        val region: Region = clipboard.region
        sender msg "&7&oDimensions&8&o:&b&o ${region.width}x${region.height}x${region.length}"
        sender msg "&aSaved schematic file ${file.absolutePath}"
        Shuttles.invalidateSchematicCache(name)
    }

    @Subcommand("schem list")
    fun onSchemList(sender: Player) {
        for (name in Shuttles.getAllSchematics()) {
            val schematic: Clipboard = readSchematic(Shuttles.getSchematicFile(name)) ?: fail { "Failed to read $name" }
            val region: Region = schematic.region
            sender msg "&6$name&8&l :: &b${region.width}x${region.height}x${region.length}"
        }
    }

    private fun validateSchematicFile(name: String): File = Shuttles.getSchematicFile(name)
        .takeIf(File::exists) ?: fail { "Shuttle schematic $name not found" }

    @Subcommand("schem load")
    @CommandCompletion("@shuttleSchematics")
    fun onSchemLoad(sender: Player, name: String) {
        val file = validateSchematicFile(name)
        val session: LocalSession = worldEditPlugin.getSession(sender)
        val clipboard: Clipboard = readSchematic(file) ?: fail { "Failed to read schematic" }
        session.clipboard = ClipboardHolder(clipboard)
        sender msg "&aCopied shuttle schematic $name to clipboard"
    }

    @Subcommand("create")
    @CommandCompletion("@nothing @shuttleSchematics")
    fun onCreate(sender: Player, name: String, schematicName: String) {
        validateName(name)
        failIf(Shuttle[name] != null) { "A shuttle named $name already existed" }
        validateSchematicFile(schematicName)
        Shuttle.create(name, schematicName)
        sender msg "Created shuttle $name"
    }

    @Subcommand("delete")
    @CommandCompletion("@shuttles")
    fun onDelete(sender: Player, shuttleName: String) {
        val shuttle = validateShuttle(shuttleName)
        if (shuttle.destinations.isNotEmpty()) {
            Shuttles.removeShuttleFromWorld(shuttle)
        }
        Shuttle.delete(shuttle._id)
        sender msg "&aDeleted shuttle $shuttleName"
    }

    @Subcommand("list")
    fun onList(sender: Player) {
        for (shuttle in Shuttle.all()) {
            sender msg "&b${shuttle.name} &d&l::&7 ${shuttle.destinations.size} &3destinations " +
                    "in worlds &2[&a${shuttle.destinations.joinToString { it.world }}&2]"
        }
    }

    private fun validateShuttle(name: String): Shuttle {
        return Shuttle[name] ?: fail { "Shuttle $name not found" }
    }

    @Subcommand("cycle")
    @CommandCompletion("@shuttles")
    fun onCycle(sender: Player, shuttleName: String) {
        val shuttle = validateShuttle(shuttleName)
        val newPosition = shuttle.nextPosition()
        Shuttles.moveShuttle(shuttle, newPosition)

        val destination = shuttle.destinations[newPosition]
        sender msg "&aMoved shuttle to $destination"
    }

    @Subcommand("set schem")
    @CommandCompletion("@shuttles @shuttleSchematics")
    fun onSetSchematic(sender: Player, shuttleName: String, schematicName: String) {
        val shuttle = validateShuttle(shuttleName)
        validateSchematicFile(schematicName)
        Shuttle.setSchematic(shuttle._id, schematicName)
        sender msg "Set schematic of shuttle $shuttleName to $schematicName"
    }

    @Subcommand("dest add")
    @CommandCompletion("@shuttles planet|station")
    fun onDestinationAdd(sender: Player, shuttleName: String, destinationName: String) {
        validateName(destinationName)
        val shuttle = validateShuttle(shuttleName)
        failIf(shuttle.destinations.any { it.name == destinationName }) {
            "Destination $destinationName already exists for $shuttleName. Shuttle data: $shuttle"
        }

        val world = sender.world
        val loc = sender.location
        val ox = loc.blockX
        val oy = loc.blockY
        val oz = loc.blockZ

        val file = validateSchematicFile(shuttle.schematic)
        val schem: Clipboard = readSchematic(file) ?: fail { "Failed to read schematic" }

        val dx = ox - schem.origin.blockX
        val dy = oy - schem.origin.blockY
        val dz = oz - schem.origin.blockZ

        for (vec in schem.region) {
            val x = vec.blockX + dx
            val y = vec.blockY + dy
            val z = vec.blockZ + dz
            failIf(getBlockTypeSafe(world, x, y, z)?.isAir != true) { "Blocked at $x $y $z" }
            val block = world.getBlockAt(x, y, z)
            world.spawnParticle(Particle.BARRIER, block.location.toCenterLocation(), 1)
        }

        Shuttle.addDestination(shuttle._id, Shuttle.Destination(destinationName, world.name, ox, oy, oz))
        sender msg "&aAdded destination $destinationName to shuttle $shuttleName"
    }

    @Subcommand("dest list")
    @CommandCompletion("@shuttles")
    fun onDestinationList(sender: Player, shuttleName: String) {
        val shuttle = validateShuttle(shuttleName)
        sender msg "&bDestinations of shuttle &e$shuttleName:"
        for (dest in shuttle.destinations) {
            sender msg "   &b::&7> &3${dest.name} &7(&a${dest.world}@[${dest.x},${dest.y},${dest.z}]&7)"
        }
    }

    @Subcommand("dest remove")
    @CommandCompletion("@shuttles @nothing")
    fun onDestinationRemove(sender: Player, shuttleName: String, destinationName: String) {
        val shuttle = validateShuttle(shuttleName)
        val destination = shuttle.destinations.firstOrNull { it.name == destinationName }
            ?: fail { "Shuttle $shuttleName does not have a destination named $destinationName! Shuttle Data: $shuttle" }

        Shuttle.removeDestination(shuttle._id, destination)

        if (shuttle.destinations.size == 1) { // if we're removing the last one
            Shuttles.removeShuttleFromWorld(shuttle)
        } else {
            // pick the first one that wasn't deleted
            val newPosition = shuttle.destinations.indexOfFirst { it.name != destinationName }
            Shuttles.moveShuttle(shuttle, newPosition)
        }

        sender msg "&aRemoved destination $destinationName from $shuttleName"
    }
}
