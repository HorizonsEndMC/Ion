package net.starlegacy.command.space

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.space.Planet
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.CachedStar
import net.starlegacy.feature.space.Orbits
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceMap
import net.starlegacy.util.randomDouble
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

@CommandAlias("planet")
@CommandPermission("space.planet")
object PlanetCommand : SLCommand() {
	@Subcommand("create")
	@CommandCompletion("@nothing true|false x z @stars @worlds 1000|2000|3000|4000|5000 1|2|3|4|5 0.5|0.75|1.0")
	fun onCreate(
		sender: CommandSender,
		name: String,
		rogue: Boolean,
		x: Int,
		z: Int,
		sun: CachedStar,
		planetWorldName: String,
		orbitDistance: Int,
		orbitSpeed: Double,
		size: Double
	) {
		if (size <= 0 || size > 1) {
			throw InvalidCommandArgument("Size must be more than 0 and no more than 1")
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

		Planet.create(
			name,
			rogue,
			x,
			z,
			sun.databaseId,
			planetWorldName,
			size,
			orbitDistance,
			orbitSpeed,
			orbitProgress,
			seed
		)

		Space.reload()

		val planet: CachedPlanet = Space.planetNameCache[name].get()

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Created planet {0} at {1} ", name, planet.location)
	}

	@Suppress("Unused")
	@Subcommand("set seed")
	@CommandCompletion("@planets 0")
	fun onSetSeed(sender: CommandSender, planet: CachedPlanet, newSeed: Long) {
		Planet.setSeed(planet.databaseId, newSeed)

		val planetName: String = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated seed in database, reloaded systems, and regenerated planet."
		)
	}

	@Suppress("Unused")
	@Subcommand("set atmosphere materials")
	@CommandCompletion("@planets @nothing")
	fun onSetAtmosphereMaterials(sender: CommandSender, planet: CachedPlanet, newMaterials: String) {
		val materials: List<String> = try {
			newMaterials.split(" ")
				.map { "${Material.valueOf(it)}" }
		} catch (exception: Exception) {
			exception.printStackTrace()
			throw InvalidCommandArgument("An error occurred parsing materials, try again")
		}

		Planet.setCloudMaterials(planet.databaseId, materials)

		val planetName: String = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated atmosphere materials in database, reloaded systems, and regenerated planet."
		)
	}

	@Suppress("Unused")
	@Subcommand("set atmosphere density")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetAtmosphereDensity(sender: CommandSender, planet: CachedPlanet, newDensity: Double) {
		Planet.setCloudDensity(planet.databaseId, newDensity)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated atmosphere density in database, reloaded systems, and regenerated planet."
		)
	}

	@Suppress("Unused")
	@Subcommand("set atmosphere noise")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetAtmosphereNoise(sender: CommandSender, planet: CachedPlanet, newNoise: Double) {
		Planet.setCloudDensityNoise(planet.databaseId, newNoise)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated atmosphere noise in database, reloaded systems, and regenerated planet."
		)
	}

	@Suppress("Unused")
	@Subcommand("set cloud threshold")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetCloudThreshold(sender: CommandSender, planet: CachedPlanet, newThreshold: Double) {
		Planet.setCloudThreshold(planet.databaseId, newThreshold)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated cloud density in database, reloaded systems, and regenerated planet."
		)
	}

	@Suppress("Unused")
	@Subcommand("set cloud noise")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetCloudNoise(sender: CommandSender, planet: CachedPlanet, newNoise: Double) {
		Planet.setCloudNoise(planet.databaseId, newNoise)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated cloud noise in database, reloaded systems, and regenerated planet."
		)
	}

	@Suppress("Unused")
	@Subcommand("set crust noise")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetCrustNoise(sender: CommandSender, planet: CachedPlanet, newNoise: Double) {
		Planet.setCrustNoise(planet.databaseId, newNoise)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated crust noise in database, reloaded systems, and regenerated planet."
		)
	}

	@Suppress("Unused")
	@Subcommand("set crust materials")
	@CommandCompletion("@planets @nothing")
	fun onSetCrustMaterials(sender: CommandSender, planet: CachedPlanet, newMaterials: String) {
		val materials: List<String> = try {
			newMaterials.split(" ")
				.map { "${Material.valueOf(it)}" }
		} catch (exception: Exception) {
			exception.printStackTrace()
			throw InvalidCommandArgument("An error occurred parsing materials, try again")
		}

		Planet.setCrustMaterials(planet.databaseId, materials)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated crust materials in database, reloaded systems, and regenerated planet."
		)
	}

	@Suppress("Unused")
	@Subcommand("set sun")
	@CommandCompletion("@planets @stars")
	fun onSetSun(sender: CommandSender, planet: CachedPlanet, newSun: CachedStar) {
		val oldSun = planet.sun
		planet.changeSun(newSun)
		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated sun from {0} to {1}, moved the planet, and updated database",
			oldSun.name,
			newSun.name
		)
	}

	@Suppress("Unused")
	@Subcommand("set rogue")
	@CommandCompletion("@planets true|false")
	fun onSetRogue(sender: CommandSender, planet: CachedPlanet, newValue: Boolean) {
		val oldValue = planet.rogue
		val spaceWorld = planet.spaceWorld ?: throw InvalidCommandArgument("That planet's space world isn't loaded!")

		planet.toggleRogue(newValue)
		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated {0} rogue to {1} from {2}",
			planet.name,
			newValue,
			oldValue
		)
		planet.setLocation(true)
		spaceWorld.save()
		SpaceMap.refresh()
	}

	@Suppress("Unused")
	@Subcommand("set location")
	@CommandCompletion("@planets x z")
	fun onSetLocation(sender: CommandSender, planet: CachedPlanet, x: Int, z: Int) {
		val spaceWorld = planet.spaceWorld ?: throw InvalidCommandArgument("That planet's space world isn't loaded!")

		planet.changeX(x)
		planet.changeZ(z)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Moved {0} to {1}, {2}", planet.name, x, z)
		planet.setLocation(true, true)
		spaceWorld.save()
		SpaceMap.refresh()
	}

	@Suppress("Unused")
	@Subcommand("set orbit distance")
	@CommandCompletion("@planets @nothing")
	fun onSetOrbitDistance(sender: CommandSender, planet: CachedPlanet, newDistance: Int) {
		val oldDistance = planet.orbitDistance
		planet.changeOrbitDistance(newDistance)
		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated distance from {0} to {1}, moved the planet, and updated database",
			oldDistance,
			newDistance
		)
	}

	@Suppress("Unused")
	@Subcommand("getpos")
	@CommandCompletion("@planets")
	fun onGetPos(sender: CommandSender, planet: CachedPlanet) {
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"{0} is at {1} in {2}. Its planet world is {3}",
			planet.name,
			planet.location,
			planet.spaceWorldName,
			planet.planetWorldName
		)
	}

	@Suppress("Unused")
	@Subcommand("info")
	@CommandCompletion("@planets")
	fun onInfo(sender: CommandSender, planet: CachedPlanet) {
		sender.sendRichMessage(
			"<dark_green>${planet.name}" +
				"  <gray>Sun: <aqua>${planet.sun.name}" +
				"  <gray>Space World: <aqua>${planet.spaceWorldName}" +
				"  <gray>Planet World: <aqua>${planet.planetWorldName}" +
				"  <gray>Rogue: <aqua>${planet.rogue}" +
				"  <gray>Fixed location: <aqua>${planet.x}, ${planet.z}" +
				"  <gray>Size: <aqua>${planet.size}" +
				"  <gray>Atmosphere Density: <aqua>${planet.cloudDensity}" +
				"  <gray>Atmosphere Radius: <aqua>${planet.atmosphereRadius}" +
				"  <gray>Atmosphere Materials: <aqua>${planet.cloudMaterials}" +
				"  <gray>Crust Radius: <aqua>${planet.crustRadius}" +
				"  <gray>Crust Materials: <aqua>${planet.crustMaterials}"
		)
	}

	@Suppress("Unused")
	@Subcommand("orbit")
	@CommandCompletion("@planets")
	fun onOrbit(sender: CommandSender, planet: CachedPlanet) {
		val spaceWorld = planet.spaceWorld ?: throw InvalidCommandArgument("That planet's space world isn't loaded!")

		val elapsedNanos = measureNanoTime {
			if (planet.rogue) {
				planet.setLocation(true)
			} else {
				planet.orbit(true)
			}
		}
		spaceWorld.save()
		SpaceMap.refresh()

		val elapsedMilliseconds = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Orbited {0} in {1}ms",
			planet.name,
			elapsedMilliseconds
		)
	}

	@Suppress("Unused")
	@Subcommand("orbit all")
	fun onOrbitAll(sender: CommandSender) {
		val elapsedNanos = measureNanoTime {
			Orbits.orbitPlanets(true)
		}

		val elapsedMilliseconds = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Orbited everything in {0}ms", elapsedMilliseconds)
	}

	@Suppress("Unused")
	@Subcommand("generate")
	@CommandCompletion("@planets")
	fun onGenerate(sender: CommandSender, planet: CachedPlanet) {
		val spaceWorld = planet.spaceWorld ?: throw InvalidCommandArgument("That planet's space world isn't loaded!")

		val elapsedNanos = measureNanoTime {
			planet.generate()
			spaceWorld.save()
			SpaceMap.refresh()
		}

		val elapsedMilliseconds = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Generated {0} in {1}ms",
			planet.name,
			elapsedMilliseconds
		)
	}

	@Suppress("Unused")
	@Subcommand("teleport|tp")
	@CommandCompletion("@planets")
	fun onTeleport(sender: Player, planet: CachedPlanet) {
		val world = planet.spaceWorld ?: throw ConditionFailedException("Planet's space world is not loaded!")

		val location = planet.location.toLocation(world).apply {
			y = world.maxHeight.toDouble()
		}

		sender.teleport(location)

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Teleported to {0}", planet.name)
	}

	@Subcommand("delete")
	@CommandCompletion("@planets")
	fun onDelete(sender: CommandSender, planet: CachedPlanet) {
		planet.erase()
		Planet.delete(planet.databaseId)
		Space.reload()
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Deleted planet {0}", planet.name)
	}
}
