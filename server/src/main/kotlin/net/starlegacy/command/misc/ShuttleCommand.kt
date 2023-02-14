package net.starlegacy.command.misc

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
import net.horizonsend.ion.server.extensions.FeedbackType
import net.horizonsend.ion.server.extensions.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.misc.Shuttle
import net.starlegacy.feature.misc.Shuttles
import net.starlegacy.util.getBlockTypeSafe
import net.starlegacy.util.isAlphanumeric
import net.starlegacy.util.readSchematic
import net.starlegacy.util.writeSchematic
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.Locale

@CommandAlias("shuttle")
@CommandPermission("starlegacy.shuttle.admin")
object ShuttleCommand : SLCommand() {
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
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"Dimensions: {0}x{1}x{2}",
			region.width,
			region.height,
			region.length
		)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Saved schematic file {0}", file.absolutePath)
		Shuttles.invalidateSchematicCache(name)
	}

	@Suppress("Unused")
	@Subcommand("schem list")
	fun onSchemList(sender: Player) {
		for (name in Shuttles.getAllSchematics()) {
			val schematic: Clipboard = readSchematic(Shuttles.getSchematicFile(name)) ?: fail { "Failed to read $name" }
			val region: Region = schematic.region
			sender.sendFeedbackMessage(
				FeedbackType.INFORMATION,
				"{0} :: {1}x{2}x{3}",
				name,
				region.width,
				region.height,
				region.length
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
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Copied shuttle schematic {0} to clipboard", name)
	}

	@Subcommand("create")
	@CommandCompletion("@nothing @shuttleSchematics")
	fun onCreate(sender: Player, name: String, schematicName: String) {
		validateName(name)
		failIf(Shuttle[name] != null) { "A shuttle named $name already existed" }
		validateSchematicFile(schematicName)
		Shuttle.create(name, schematicName)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Created shuttle {0}", name)
	}

	@Subcommand("delete")
	@CommandCompletion("@shuttles")
	fun onDelete(sender: Player, shuttleName: String) {
		val shuttle = validateShuttle(shuttleName)
		if (shuttle.destinations.isNotEmpty()) {
			Shuttles.removeShuttleFromWorld(shuttle)
		}
		Shuttle.delete(shuttle._id)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Deleted shuttle {0}", shuttleName)
	}

	@Suppress("Unused")
	@Subcommand("list")
	fun onList(sender: Player) {
		for (shuttle in Shuttle.all()) {
			sender.sendFeedbackMessage(
				FeedbackType.INFORMATION,
				"{0} :: {1} destinations in worlds [{2}]",
				shuttle.name,
				shuttle.destinations.size,
				shuttle.destinations.joinToString { it.world }
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
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Moved shuttle to {0}", destination)
	}

	@Suppress("Unused")
	@Subcommand("set schem")
	@CommandCompletion("@shuttles @shuttleSchematics")
	fun onSetSchematic(sender: Player, shuttleName: String, schematicName: String) {
		val shuttle = validateShuttle(shuttleName)
		validateSchematicFile(schematicName)
		Shuttle.setSchematic(shuttle._id, schematicName)
		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Set schematic of shuttle {0} to {1}",
			shuttleName,
			schematicName
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
		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Added destination {0} to shuttle {1}",
			destinationName,
			shuttleName
		)
	}

	@Suppress("Unused")
	@Subcommand("dest list")
	@CommandCompletion("@shuttles")
	fun onDestinationList(sender: Player, shuttleName: String) {
		val shuttle = validateShuttle(shuttleName)
		sender.sendFeedbackMessage(FeedbackType.INFORMATION, "Destinations of shuttle {0}:", shuttleName)
		for (dest in shuttle.destinations) {
			sender.sendFeedbackMessage(
				FeedbackType.INFORMATION,
				"   ::> {0} ({1}@[{2},{3},{4}])",
				dest.name,
				dest.world,
				dest.x,
				dest.y,
				dest.z
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

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Removed destination {0} from {1}",
			destinationName,
			shuttleName
		)
	}
}
