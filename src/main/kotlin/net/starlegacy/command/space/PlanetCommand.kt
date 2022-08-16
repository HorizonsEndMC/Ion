package net.starlegacy.command.space

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.space.Planet
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.CachedStar
import net.starlegacy.feature.space.Orbits
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.space.SpaceMap
import net.starlegacy.util.green
import net.starlegacy.util.msg
import net.starlegacy.util.randomDouble
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("planet")
@CommandPermission("space.planet")
object PlanetCommand : SLCommand() {
	@Subcommand("create")
	@CommandCompletion("@nothing @true|false @nothing @nothing @stars @worlds 1000|2000|3000|4000|5000 1|2|3|4|5 0.5|0.75|1.0")
	fun onCreate(
		sender: CommandSender,
		name: String,
		rogue: Boolean,
		x: Int,
		z: Int,
		sun: CachedStar,
		planetWorldName: String,
		orbitDistance: Int, orbitSpeed: Double,
		size: Double
	) {
		if (size <= 0 || size > 1) {
			throw InvalidCommandArgument("Size must be more than 0 and no more than 1")
		}

		if (Space.getPlanet(name) != null) {
			throw InvalidCommandArgument("A star with that name already exists!")
		}

		val seed: Long = name.hashCode().toLong()
		val orbitProgress: Double = randomDouble(0.0, 360.0)

		Planet.create(name, rogue, x, z, sun.databaseId, planetWorldName, size, orbitDistance, orbitSpeed, orbitProgress, seed)

		Space.reload()

		val planet: CachedPlanet = Space.planetNameCache[name].get()

		sender msg green("Created planet $name at ${planet.location} ")
	}

	@Subcommand("set seed")
	@CommandCompletion("@planets 0")
	fun onSetSeed(sender: CommandSender, planet: CachedPlanet, newSeed: Long) {
		Planet.setSeed(planet.databaseId, newSeed)

		val planetName: String = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender msg green("Updated seed in database, reloaded systems, and regenerated planet.")
	}

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

		sender msg "Updated atmosphere materials in database, reloaded systems, and regenerated planet."
	}

	@Subcommand("set atmosphere density")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetAtmosphereDensity(sender: CommandSender, planet: CachedPlanet, newDensity: Double) {
		Planet.setCloudDensity(planet.databaseId, newDensity)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender msg green("Updated atmosphere density in database, reloaded systems, and regenerated planet.")
	}

	@Subcommand("set atmosphere noise")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetAtmosphereNoise(sender: CommandSender, planet: CachedPlanet, newNoise: Double) {
		Planet.setCloudDensityNoise(planet.databaseId, newNoise)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender msg green("Updated atmosphere noise in database, reloaded systems, and regenerated planet.")
	}

	@Subcommand("set cloud threshold")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetCloudThreshold(sender: CommandSender, planet: CachedPlanet, newThreshold: Double) {
		Planet.setCloudThreshold(planet.databaseId, newThreshold)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender msg green("Updated cloud density in database, reloaded systems, and regenerated planet.")
	}

	@Subcommand("set cloud noise")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetCloudNoise(sender: CommandSender, planet: CachedPlanet, newNoise: Double) {
		Planet.setCloudNoise(planet.databaseId, newNoise)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender msg green("Updated cloud noise in database, reloaded systems, and regenerated planet.")
	}

	@Subcommand("set crust noise")
	@CommandCompletion("@planets 0.1|0.2|0.3|0.4|0.5")
	fun onSetCrustNoise(sender: CommandSender, planet: CachedPlanet, newNoise: Double) {
		Planet.setCrustNoise(planet.databaseId, newNoise)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender msg green("Updated crust noise in database, reloaded systems, and regenerated planet.")
	}

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

		sender msg "Updated crust materials in database, reloaded systems, and regenerated planet."
	}

	@Subcommand("set sun")
	@CommandCompletion("@planets @stars")
	fun onSetSun(sender: CommandSender, planet: CachedPlanet, newSun: CachedStar) {
		val oldSun = planet.sun
		planet.changeSun(newSun)
		sender msg green("Updated sun from ${oldSun.name} to ${newSun.name}, moved the planet, and updated database")
	}

	@Subcommand("set rogue")
	@CommandCompletion("@planets true|false")
	fun onSetRogue(sender: CommandSender, planet: CachedPlanet, newValue: Boolean) {
		val oldValue = planet.rogue
		planet.toggleRogue(newValue)
		sender msg green("Updated ${planet.name} rogue to $newValue from $oldValue")
	}

	@Subcommand("set location")
	@CommandCompletion("@nothing @nothing")
	fun onSetLocation(sender: CommandSender, planet: CachedPlanet, x: Int, z: Int) {
		planet.changeX(x)
		planet.changeZ(z)
		sender msg green ("Moved ${planet.name} to $x, $z")
		planet.setLocation(true)
	}

	@Subcommand("set orbit distance")
	@CommandCompletion("@planets @nothing")
	fun onSetOrbitDistance(sender: CommandSender, planet: CachedPlanet, newDistance: Int) {
		val oldDistance = planet.orbitDistance
		planet.changeOrbitDistance(newDistance)
		sender msg green("Updated distance from $oldDistance to $newDistance, moved the planet, and updated database")
	}

	@Subcommand("getpos")
	@CommandCompletion("@planets")
	fun onGetPos(sender: CommandSender, planet: CachedPlanet) {
		sender msg "&7${planet.name}&b is at &e${planet.location}&b in &c${planet.spaceWorldName}&b. " +
			"Its planet world is &2${planet.planetWorldName}"
	}

	@Subcommand("info")
	@CommandCompletion("@planets")
	fun onInfo(sender: CommandSender, planet: CachedPlanet) {
		sender msg "&2${planet.name}"
		sender msg "  &7Sun:&b ${planet.sun.name}"
		sender msg "  &7Space World:&b ${planet.spaceWorldName}"
		sender msg "  &7Planet World:&b ${planet.planetWorldName}"
		sender msg "  &7Size:&b ${planet.size}"
		sender msg "  &7Atmosphere Density:&b ${planet.cloudDensity}"
		sender msg "  &7Atmosphere Radius:&b ${planet.atmosphereRadius}"
		sender msg "  &7Atmosphere Materials:&b ${planet.cloudMaterials}"
		sender msg "  &7Crust Radius:&b ${planet.crustRadius}"
		sender msg "  &7Crust Materials:&b ${planet.crustMaterials}"
	}

	@Subcommand("orbit")
	@CommandCompletion("@planets")
	fun onOrbit(sender: CommandSender, planet: CachedPlanet) {
		val spaceWorld = planet.spaceWorld ?: throw InvalidCommandArgument("That planet's space world isn't loaded!")

		val elapsedNanos = measureNanoTime {
			if(planet.rogue) {
				planet.setLocation(true)
				spaceWorld.save()
				SpaceMap.refresh()
			}

			if(!planet.rogue) {
			planet.orbit(true)
			spaceWorld.save()
			SpaceMap.refresh()
			}
		}

		val elapsedMilliseconds = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)

		sender msg "&7Orbited &b${planet.name}&7 in &c${elapsedMilliseconds}ms"
	}

	@Subcommand("orbit all")
	fun onOrbitAll(sender: CommandSender) {
		val elapsedNanos = measureNanoTime {
			Orbits.orbitPlanets(true)
		}

		val elapsedMilliseconds = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)

		sender msg "&7Orbited &b&oeverything&7 in &c${elapsedMilliseconds}ms"
	}

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

		sender msg "&7Generated &b${planet.name}&7 in &c${elapsedMilliseconds}ms"
	}

	@Subcommand("teleport|tp")
	@CommandCompletion("@planets")
	fun onTeleport(sender: Player, planet: CachedPlanet) {
		val world = planet.spaceWorld ?: throw ConditionFailedException("Planet's space world is not loaded!")

		val location = planet.location.toLocation(world).apply {
			y = world.maxHeight.toDouble()
		}

		sender.teleport(location)

		sender msg green("Teleported to ${planet.name}")
	}

	@Subcommand("delete")
	@CommandCompletion("@planets")
	fun onDelete(sender: CommandSender, planet: CachedPlanet) {
		planet.erase()
		Planet.delete(planet.databaseId)
		Space.reload()
		sender msg "&aDeleted planet ${planet.name}"
	}
}
