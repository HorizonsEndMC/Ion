package net.horizonsend.ion.server.command.space

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.space.Star
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.OrbitingCelestialBody
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.orNull
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("star")
@CommandPermission("space.star")
object StarCommand : net.horizonsend.ion.server.command.SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(CachedStar::class.java) { c: BukkitCommandExecutionContext ->
			Space.starNameCache[c.popFirstArg().uppercase(Locale.getDefault())].orNull()
				?: throw InvalidCommandArgument("No such star")
		}
	}

	@Subcommand("create")
	@CommandCompletion("@nothing @worlds @nothing @nothing SEA_LANTERN|GLOWSTONE|MAGMA @nothing")
	fun onCreate(
		sender: CommandSender,
		name: String,
		spaceWorld: World,
		x: Int,
		z: Int,
		size: Double
	) {
		if (!spaceWorld.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
			throw InvalidCommandArgument("Not a space world!")
		}

		if (size <= 0 || size > 1) {
			throw InvalidCommandArgument("Size must be more than 0 and no more than 1")
		}

		if (Space.getStar(name) != null) {
			throw InvalidCommandArgument("A star with that name already exists!")
		}

		val seed: Long = name.hashCode().toLong()
		Star.create(name, spaceWorld.name, x, 128, z, size, seed)

		Space.reload()

		Space.starNameCache[name].get().generate()

		sender.success(
			"Created star $name at $x $z in $spaceWorld with size $size"
		)
	}

	@Suppress("Unused")
	@Subcommand("getpos")
	@CommandCompletion("@stars")
	fun onGetPos(sender: CommandSender, star: CachedStar) {
		sender.information(
			"${star.name} is at ${star.location} in ${star.spaceWorldName}."
		)
	}

	@Suppress("Unused")
	@Subcommand("teleport|tp")
	@CommandCompletion("@stars")
	fun onTeleport(sender: Player, star: CachedStar) {
		val world = star.spaceWorld ?: throw ConditionFailedException("Star's space world is not loaded!")

		val location = star.location.toLocation(world).apply {
			y = world.maxHeight.toDouble()
		}

		sender.teleport(location)

		sender.success("Teleported to ${star.name}")
	}

	@Suppress("Unused")
	@Subcommand("generate")
	@CommandCompletion("@stars")
	fun onGenerate(sender: CommandSender, star: CachedStar) {
		sender.information("Generating star...")
		star.generate()
		sender.success("Generated star ${star.name}")
	}

	@Suppress("Unused")
	@Subcommand("move")
	@CommandCompletion("@stars @nothing @nothing")
	fun onMove(sender: CommandSender, star: CachedStar, spaceWorld: World, newX: Int, newZ: Int) {
		moveStar(sender, newX, newZ, star, spaceWorld)
		moveOrbitingPlanets(sender, star, spaceWorld)

		Star.setPos(star.databaseId, spaceWorld.name, newX, 128, newZ)
		sender.success("Moved star ${star.name} to $newX, $newZ")
	}

	private fun moveStar(sender: CommandSender, newX: Int, newZ: Int, star: CachedStar, spaceWorld: World) {
		sender.information("Moving star...")

		val newLoc = Vec3i(newX, 128, newZ)
		star.move(newLoc, spaceWorld)
	}

	private fun moveOrbitingPlanets(sender: CommandSender, star: CachedStar, spaceWorld: World) {
		sender.information("Moving orbiting planets...")

		for (planet in Space.getOrbitingPlanets()) {
			if (planet.sun.databaseId == star.databaseId) {
				sender.information("Moving ${planet.name}...")
				val newLoc = OrbitingCelestialBody.calculateOrbitLocation(star.location, planet.orbitDistance, planet.orbitProgress)
				planet.move(newLoc, spaceWorld)
			}
		}
	}

	@Suppress("Unused")
	@Subcommand("set size")
	@CommandCompletion("@stars @nothing @nothing")
	fun onSetSize(sender: CommandSender, star: CachedStar, size: Double) {
		Star.setSize(star.databaseId, size)

		val starName = star.name
		Space.reload()
		Space.starNameCache[starName].get().generate()

		sender.success(
			"Updated crust materials in database, reloaded systems, and regenerated star."
		)
	}

	@Suppress("Unused")
	@Subcommand("set crust layer")
	@CommandCompletion("@stars @nothing @nothing")
	fun onSetCrustLayer(sender: CommandSender, star: CachedStar, index: Int, noise: Double, newMaterials: String) {
		val materials: List<String> = try {
			newMaterials.split(" ")
				.map { "${Material.valueOf(it)}" }
		} catch (exception: Exception) {
			exception.printStackTrace()
			throw InvalidCommandArgument("An error occurred parsing materials, try again")
		}

		val material = Star.Companion.CrustLayer(index, noise,  materials)
		Star.setCrustLayer(star.databaseId, material)

		val starName = star.name
		Space.reload()
		Space.starNameCache[starName].get().generate()

		sender.success(
			"Updated crust materials in database, reloaded systems, and regenerated star."
		)
	}

	@Suppress("Unused")
	@Subcommand("remove crust layer")
	@CommandCompletion("@stars @nothing @nothing")
	fun onRemoveCrustLayer(sender: CommandSender, star: CachedStar, index: Int) {
		Star.removeCrustLayer(star.databaseId, index)

		val starName = star.name
		Space.reload()
		Space.starNameCache[starName].get().generate()

		sender.success(
			"Updated crust materials in database, reloaded systems, and regenerated star."
		)
	}

	@Suppress("Unused")
	@Subcommand("clear crust")
	@CommandCompletion("@stars @nothing @nothing")
	fun onClearCrustLayer(sender: CommandSender, star: CachedStar) {
		Star.clearCrustLayers(star.databaseId)

		val starName = star.name
		Space.reload()
		Space.starNameCache[starName].get().generate()

		sender.success(
			"Updated crust materials in database, reloaded systems, and regenerated star."
		)
	}
}
