package net.starlegacy.command.space

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.extensions.FeedbackType
import net.horizonsend.ion.server.extensions.information
import net.horizonsend.ion.server.extensions.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.space.Star
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.CachedStar
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.util.Vec3i
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("star")
@CommandPermission("space.star")
object StarCommand : SLCommand() {
	@Subcommand("create")
	@CommandCompletion("@nothing @worlds @nothing @nothing SEA_LANTERN|GLOWSTONE|MAGMA @nothing")
	fun onCreate(
		sender: CommandSender,
		name: String,
		spaceWorld: World,
		x: Int,
		z: Int,
		material: Material,
		size: Double
	) {
		if (!SpaceWorlds.contains(spaceWorld)) {
			throw InvalidCommandArgument("Not a space world!")
		}

		if (size <= 0 || size > 1) {
			throw InvalidCommandArgument("Size must be more than 0 and no more than 1")
		}

		if (Space.getStar(name) != null) {
			throw InvalidCommandArgument("A star with that name already exists!")
		}

		Star.create(name, spaceWorld.name, x, 128, z, material.name, size)

		Space.reload()

		Space.starNameCache[name].get().generate()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Created star {0} at {1} {2} in {3} with material {4} and size {5}",
			name,
			x,
			z,
			spaceWorld,
			material,
			size
		)
	}

	@Suppress("Unused")
	@Subcommand("getpos")
	@CommandCompletion("@stars")
	fun onGetPos(sender: CommandSender, star: CachedStar) {
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"{0} is at {1} in {2}.",
			star.name,
			star.location,
			star.spaceWorldName
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

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Teleported to {0}", star.name)
	}

	@Suppress("Unused")
	@Subcommand("generate")
	@CommandCompletion("@stars")
	fun onGenerate(sender: CommandSender, star: CachedStar) {
		sender.information("Generating star...")
		star.generate()
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Generated star {0}", star.name)
	}

	@Suppress("Unused")
	@Subcommand("move")
	@CommandCompletion("@stars @nothing @nothing")
	fun onMove(sender: CommandSender, star: CachedStar, spaceWorld: World, newX: Int, newZ: Int) {
		moveStar(sender, newX, newZ, star, spaceWorld)
		moveOrbitingPlanets(sender, star, spaceWorld)

		Star.setPos(star.databaseId, spaceWorld.name, newX, 128, newZ)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Moved star {0} to {1}, {2}", star.name, newX, newZ)
	}

	private fun moveStar(sender: CommandSender, newX: Int, newZ: Int, star: CachedStar, spaceWorld: World) {
		sender.information("Moving star...")

		val newLoc = Vec3i(newX, 128, newZ)
		star.move(newLoc, spaceWorld)
	}

	private fun moveOrbitingPlanets(sender: CommandSender, star: CachedStar, spaceWorld: World) {
		sender.information("Moving orbiting planets...")

		for (planet in Space.getPlanets()) {
			if (planet.sun.databaseId == star.databaseId) {
				sender.sendFeedbackMessage(FeedbackType.INFORMATION, "Moving {0}...", planet.name)
				val newLoc = CachedPlanet.calculateOrbitLocation(star, planet.orbitDistance, planet.orbitProgress)
				planet.move(newLoc, spaceWorld)
			}
		}
	}
}
