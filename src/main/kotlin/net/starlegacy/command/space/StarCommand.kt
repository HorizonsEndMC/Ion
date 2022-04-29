package net.starlegacy.command.space

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.space.Star
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.CachedStar
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.util.Vec3i
import net.starlegacy.util.green
import net.starlegacy.util.msg
import net.starlegacy.util.yellow
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
		spaceWorld: World, x: Int, z: Int,
		material: Material, size: Double
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

		sender msg green("Created star $name at $x $z in $spaceWorld with material $material and size $size")
	}


	@Subcommand("getpos")
	@CommandCompletion("@stars")
	fun onGetPos(sender: CommandSender, star: CachedStar) {
		sender msg "&7${star.name}&b is at &e${star.location}&b in &c${star.spaceWorldName}&b."
	}

	@Subcommand("teleport|tp")
	@CommandCompletion("@stars")
	fun onTeleport(sender: Player, star: CachedStar) {
		val world = star.spaceWorld ?: throw ConditionFailedException("Star's space world is not loaded!")

		val location = star.location.toLocation(world).apply {
			y = world.maxHeight.toDouble()
		}

		sender.teleport(location)

		sender msg green("Teleported to ${star.name}")
	}

	@Subcommand("generate")
	@CommandCompletion("@stars")
	fun onGenerate(sender: CommandSender, star: CachedStar) {
		sender msg yellow("Generating star...")
		star.generate()
		sender msg green("Generated star ${star.name}")
	}

	@Subcommand("move")
	@CommandCompletion("@stars @nothing @nothing")
	fun onMove(sender: CommandSender, star: CachedStar, spaceWorld: World, newX: Int, newZ: Int) {
		moveStar(sender, newX, newZ, star, spaceWorld)
		moveOrbitingPlanets(sender, star, spaceWorld)

		Star.setPos(star.databaseId, spaceWorld.name, newX, 128, newZ)
		sender msg "Moved star ${star.name} to $newX, $newZ"
	}

	private fun moveStar(sender: CommandSender, newX: Int, newZ: Int, star: CachedStar, spaceWorld: World) {
		sender msg yellow("Moving star...")

		val newLoc = Vec3i(newX, 128, newZ)
		star.move(newLoc, spaceWorld)
	}

	private fun moveOrbitingPlanets(sender: CommandSender, star: CachedStar, spaceWorld: World) {
		sender msg yellow("Moving orbiting planets...")

		for (planet in Space.getPlanets()) {
			if (planet.sun.databaseId == star.databaseId) {
				sender msg yellow("Moving ${planet.name}...")
				val newLoc = CachedPlanet.calculateLocation(star, planet.orbitDistance, planet.orbitProgress)
				planet.move(newLoc, spaceWorld)
			}
		}
	}
}
