package net.starlegacy.command.space

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.database.schema.space.Moon
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.space.CachedMoon
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceMap
import net.starlegacy.util.randomDouble
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

@CommandAlias("moon")
@CommandPermission("space.planet")
object MoonCommand : SLCommand() {
	@Subcommand("create")
	@CommandCompletion("@nothing @planets @worlds 1000|2000|3000|4000|5000 1|2|3|4|5 0.5|0.75|1.0")
	fun onCreate(
		sender: CommandSender,
		name: String,
		sun: CachedPlanet,
		planetWorldName: String,
		orbitDistance: Int,
		orbitSpeed: Double,
		size: Double
	) {
		if (size < 0 || size > 1) {
			throw InvalidCommandArgument("Size must be more than 0 and no more than 1: $size")
		}

		if (Space.getPlanet(name) != null) {
			throw InvalidCommandArgument("A star with that name already exists!")
		}
		if (Space.getPlanet(name)?.rogue == true) {
			val planet: CachedPlanet = Space.planetNameCache[name].get()
			planet.setLocation(true)
		}

		val seed: Long = name.hashCode().toLong()
		val orbitProgress: Double = randomDouble(0.0, 360.0)

		Moon.create(
			name = name,
			parent = sun.databaseId,
			planetWorld = planetWorldName,
			orbitDistance = orbitDistance,
			size = size,
			orbitSpeed = orbitSpeed,
			orbitProgress = orbitProgress,
			seed = seed
		)

		Space.reload()

		val planet: CachedMoon = Space.moonNameCache[name].get()

		sender.success("Created planet $name at ${planet.location}")
	}

	@Suppress("Unused")
	@Subcommand("set seed")
	@CommandCompletion("@moons 0")
	fun onSetSeed(sender: CommandSender, moon: CachedMoon, newSeed: Long) {
		Moon.setSeed(moon.databaseId, newSeed)

		val moonName: String = moon.name
		Space.reload()
		Space.moonNameCache[moonName].get().generate()

		sender.success(
			"Updated seed in database, reloaded systems, and regenerated moon."
		)
	}

	@Suppress("Unused")
	@Subcommand("set crust noise")
	@CommandCompletion("@moons 0.1|0.2|0.3|0.4|0.5")
	fun onSetCrustNoise(sender: CommandSender, moon: CachedMoon, newNoise: Double) {
		Moon.setCrustNoise(moon.databaseId, newNoise)

		val moonName = moon.name
		Space.reload()
		Space.moonNameCache[moonName].get().generate()

		sender.success(
			"Updated crust noise in database, reloaded systems, and regenerated moon."
		)
	}

	@Suppress("Unused")
	@Subcommand("set crust materials")
	@CommandCompletion("@moons @nothing")
	fun onSetCrustMaterials(sender: CommandSender, moon: CachedMoon, newMaterials: String) {
		val materials: List<String> = try {
			newMaterials.split(" ")
				.map { "${Material.valueOf(it)}" }
		} catch (exception: Exception) {
			exception.printStackTrace()
			throw InvalidCommandArgument("An error occurred parsing materials, try again")
		}

		Moon.setCrustMaterials(moon.databaseId, materials)

		val moonName = moon.name
		Space.reload()
		Space.moonNameCache[moonName].get().generate()

		sender.success(
			"Updated crust materials in database, reloaded systems, and regenerated moon."
		)
	}

	@Suppress("Unused")
	@Subcommand("set sun")
	@CommandCompletion("@moons @stars")
	fun onSetSun(sender: CommandSender, moon: CachedMoon, newParent: CachedPlanet) {
		val oldSun = moon.parent
		moon.changeSun(newParent)
		sender.success(
			"Updated sun from ${oldSun.name} to ${newParent.name}, moved the moon, and updated database"
		)
	}

	@Suppress("Unused")
	@Subcommand("set orbitpos")
	@CommandCompletion("@moons position")
	fun onSetOrbitPos(sender: CommandSender, moon: CachedMoon, newValue: Double) {
		val oldValue = moon.orbitProgress
		val spaceWorld = moon.spaceWorld ?: throw InvalidCommandArgument("That moon's space world isn't loaded!")

		moon.setOrbitProgress(newValue)
		sender.success(
			"Updated ${moon.name} orbit progress from $oldValue to $newValue"
		)
		moon.orbit(true)
		spaceWorld.save()
		SpaceMap.refresh()
	}

	@Suppress("Unused")
	@Subcommand("set orbit distance")
	@CommandCompletion("@moons @nothing")
	fun onSetOrbitDistance(sender: CommandSender, moon: CachedMoon, newDistance: Int) {
		val oldDistance = moon.orbitDistance
		moon.changeOrbitDistance(newDistance)
		sender.success(
			"Updated distance from $oldDistance to $newDistance, moved the moon, and updated database"
		)
	}

	@Suppress("Unused")
	@Subcommand("getpos")
	@CommandCompletion("@moons")
	fun onGetPos(sender: CommandSender, moon: CachedMoon) {
		sender.information(
			"${moon.name} is at ${moon.location} in ${moon.spaceWorldName}. " +
				"Its moon world is ${moon.worldName}"
		)
	}

	@Suppress("Unused")
	@Subcommand("info")
	@CommandCompletion("@moons")
	fun onInfo(sender: CommandSender, moon: CachedMoon) {
		sender.sendRichMessage(
			"<dark_green>${moon.name}\n" +
				"  <gray>Sun: <aqua>${moon.parent.name}\n" +
				"  <gray>Space World: <aqua>${moon.spaceWorldName}\n" +
				"  <gray>Moon World: <aqua>${moon.planetWorld}\n" +
				"  <gray>Size: <aqua>${moon.size}\n" +
				"  <gray>Crust Radius: <aqua>${moon.crustRadius}\n" +
				"  <gray>Crust Materials: <aqua>${moon.crustMaterials}"
		)
	}

	@Suppress("Unused")
	@Subcommand("orbit")
	@CommandCompletion("@moons")
	fun onOrbit(sender: CommandSender, moon: CachedMoon) {
		val spaceWorld = moon.spaceWorld ?: throw InvalidCommandArgument("That moon's space world isn't loaded!")

		val elapsedNanos = measureNanoTime {
			moon.orbit(true)
		}
		spaceWorld.save()
		SpaceMap.refresh()

		val elapsedMilliseconds = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)

		sender.success(
			"Orbited ${moon.name} in ${elapsedMilliseconds}ms"
		)
	}

	@Suppress("Unused")
	@Subcommand("generate")
	@CommandCompletion("@moons")
	fun onGenerate(sender: CommandSender, moon: CachedMoon) {
		val spaceWorld = moon.spaceWorld ?: throw InvalidCommandArgument("That moon's space world isn't loaded!")

		val elapsedNanos = measureNanoTime {
			moon.generate()
			spaceWorld.save()
			SpaceMap.refresh()
		}

		val elapsedMilliseconds = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)

		sender.success(
			"Generated ${moon.name} in ${elapsedMilliseconds}ms"
		)
	}

	@Suppress("Unused")
	@Subcommand("teleport|tp")
	@CommandCompletion("@moons")
	fun onTeleport(sender: Player, moon: CachedMoon) {
		val world = moon.spaceWorld ?: throw ConditionFailedException("Moon's space world is not loaded!")

		val location = moon.location.toLocation(world).apply {
			y = world.maxHeight.toDouble()
		}

		sender.teleport(location)

		sender.success("Teleported to ${moon.name}")
	}

	@Subcommand("delete")
	@CommandCompletion("@moons")
	fun onDelete(sender: CommandSender, moon: CachedMoon) {
		moon.erase()
		Moon.delete(moon.databaseId)
		Space.reload()
		sender.success("Deleted moon ${moon.name}")
	}
}
