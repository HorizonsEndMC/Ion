package net.horizonsend.ion.server.command.misc

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import com.sk89q.worldedit.EmptyClipboardException
import com.sk89q.worldedit.LocalSession
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.session.ClipboardHolder
import net.horizonsend.ion.common.database.schema.misc.Shuttle
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.isAlphanumeric
import net.horizonsend.ion.server.features.misc.Shuttles
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.horizonsend.ion.server.miscellaneous.utils.writeSchematic
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

@CommandAlias("shuttle")
@CommandPermission("starlegacy.shuttle.admin")
object ShuttleCommand : net.horizonsend.ion.server.command.SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "shuttles") { _ -> Shuttle.all().map { it.name } }
		registerAsyncCompletion(manager, "shuttleSchematics") { _ -> Shuttles.getAllSchematics() }
	}

	private val worldEditPlugin: WorldEditPlugin get() = JavaPlugin.getPlugin(WorldEditPlugin::class.java)

	private fun validateName(name: String) {
		failIf(name != name.lowercase(Locale.getDefault())) { "Name must be lowercase" }
		failIf(!name.replace("_", "").isAlphanumeric()) { "Name must use only letters, numbers, and underscores" }
	}

	@Suppress("Unused")
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
		sender.information(
			"Dimensions: ${region.width}x${region.height}x${region.length}"
		)
		sender.success("Saved schematic file ${file.absolutePath}")
		Shuttles.invalidateSchematicCache(name)
	}

	@Suppress("Unused")
	@Subcommand("schem list")
	fun onSchemList(sender: Player) {
		for (name in Shuttles.getAllSchematics()) {
			val schematic: Clipboard = readSchematic(Shuttles.getSchematicFile(name)) ?: fail { "Failed to read $name" }
			val region: Region = schematic.region
			sender.information(
				"$name :: ${region.width}x${region.height}x${region.length}"
			)
		}
	}

	private fun validateSchematicFile(name: String): File = Shuttles.getSchematicFile(name)
		.takeIf(File::exists) ?: fail { "Shuttle schematic $name not found" }

	@Suppress("Unused")
	@Subcommand("schem load")
	@CommandCompletion("@shuttleSchematics")
	fun onSchemLoad(sender: Player, name: String) {
		val file = validateSchematicFile(name)
		val session: LocalSession = worldEditPlugin.getSession(sender)
		val clipboard: Clipboard = readSchematic(file) ?: fail { "Failed to read schematic" }
		session.clipboard = ClipboardHolder(clipboard)
		sender.success("Copied shuttle schematic $name to clipboard")
	}

	@Subcommand("create")
	@CommandCompletion("@nothing @shuttleSchematics")
	fun onCreate(sender: Player, name: String, schematicName: String) {
		validateName(name)
		failIf(Shuttle[name] != null) { "A shuttle named $name already existed" }
		validateSchematicFile(schematicName)
		Shuttle.create(name, schematicName)
		sender.success("Created shuttle $name")
	}

	@Subcommand("delete")
	@CommandCompletion("@shuttles")
	fun onDelete(sender: Player, shuttleName: String) {
		val shuttle = validateShuttle(shuttleName)
		if (shuttle.destinations.isNotEmpty()) {
			Shuttles.removeShuttleFromWorld(shuttle)
		}
		Shuttle.delete(shuttle._id)
		sender.success("Deleted shuttle $shuttleName")
	}

	@Suppress("Unused")
	@Subcommand("list")
	fun onList(sender: Player) {
		for (shuttle in Shuttle.all()) {
			sender.information(
				"${shuttle.name} :: ${shuttle.destinations.size} destinations in worlds [${shuttle.destinations.joinToString { it.world }}]"
			)
		}
	}

	private fun validateShuttle(name: String): Shuttle {
		return Shuttle[name] ?: fail { "Shuttle $name not found" }
	}

	@Suppress("Unused")
	@Subcommand("cycle")
	@CommandCompletion("@shuttles")
	fun onCycle(sender: Player, shuttleName: String) {
		val shuttle = validateShuttle(shuttleName)
		val newPosition = shuttle.nextPosition()
		Shuttles.moveShuttle(shuttle, newPosition)

		val destination = shuttle.destinations[newPosition]
		sender.success("Moved shuttle to $destination")
	}

	@Suppress("Unused")
	@Subcommand("set schem")
	@CommandCompletion("@shuttles @shuttleSchematics")
	fun onSetSchematic(sender: Player, shuttleName: String, schematicName: String) {
		val shuttle = validateShuttle(shuttleName)
		validateSchematicFile(schematicName)
		Shuttle.setSchematic(shuttle._id, schematicName)
		sender.success(
			"Set schematic of shuttle $shuttleName to $schematicName"
		)
	}

	@Suppress("Unused")
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
			world.spawnParticle(Particle.BLOCK_MARKER, block.location.toCenterLocation(), 1)
		}

		Shuttle.addDestination(shuttle._id, Shuttle.Destination(destinationName, world.name, ox, oy, oz))
		sender.success(
			"Added destination $destinationName to shuttle $shuttleName"
		)
	}

	@Suppress("Unused")
	@Subcommand("dest list")
	@CommandCompletion("@shuttles")
	fun onDestinationList(sender: Player, shuttleName: String) {
		val shuttle = validateShuttle(shuttleName)
		sender.information("Destinations of shuttle $shuttleName:")
		for (dest in shuttle.destinations) {
			sender.information(
				"   ::> ${dest.name} (${dest.world}@[${dest.x},${dest.y},${dest.z}])"
			)
		}
	}

	@Suppress("Unused")
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

		sender.success(
			"Removed destination $destinationName from $shuttleName"
		)
	}
}
