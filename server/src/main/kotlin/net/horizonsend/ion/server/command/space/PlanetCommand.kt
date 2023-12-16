package net.horizonsend.ion.server.command.space

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.space.Planet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.features.space.CachedPlanet
import net.horizonsend.ion.server.features.space.CachedStar
import net.horizonsend.ion.server.features.space.Orbits
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceMap
import net.horizonsend.ion.server.miscellaneous.utils.orNull
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

@CommandAlias("planet")
@CommandPermission("space.planet")
object PlanetCommand : net.horizonsend.ion.server.command.SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(CachedPlanet::class.java) { c: BukkitCommandExecutionContext ->
			Space.planetNameCache[c.popFirstArg().uppercase(Locale.getDefault())].orNull()
				?: throw InvalidCommandArgument("No such planet")
		}

		registerAsyncCompletion(manager, "stars") { _ -> Space.getStars().map(CachedStar::name) }
		registerAsyncCompletion(manager, "planets") { _ -> Space.getPlanets().map(CachedPlanet::name) }
		registerAsyncCompletion(manager, "planetsInWorld") { e -> Space.getPlanets()
			.filter { planet -> planet.spaceWorldName == e.player.world.name }
			.map(CachedPlanet::name)
		}
	}

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

		sender.success("Created planet $name at ${planet.location}")
	}

	@Suppress("Unused")
	@Subcommand("set seed")
	@CommandCompletion("@planets 0")
	fun onSetSeed(sender: CommandSender, planet: CachedPlanet, newSeed: Long) {
		Planet.setSeed(planet.databaseId, newSeed)

		val planetName: String = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.success(
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

		sender.success(
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

		sender.success(
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

		sender.success(
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

		sender.success(
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

		sender.success(
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

		sender.success(
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

		sender.success(
			"Updated crust materials in database, reloaded systems, and regenerated planet."
		)
	}

	@Suppress("Unused")
	@Subcommand("set sun")
	@CommandCompletion("@planets @stars")
	fun onSetSun(sender: CommandSender, planet: CachedPlanet, newSun: CachedStar) {
		val oldSun = planet.sun
		planet.changeSun(newSun)
		sender.success(
			"Updated sun from ${oldSun.name} to ${newSun.name}, moved the planet, and updated database"
		)
	}

	@Suppress("Unused")
	@Subcommand("set rogue")
	@CommandCompletion("@planets true|false")
	fun onSetRogue(sender: CommandSender, planet: CachedPlanet, newValue: Boolean) {
		val oldValue = planet.rogue
		val spaceWorld = planet.spaceWorld ?: throw InvalidCommandArgument("That planet's space world isn't loaded!")

		planet.toggleRogue(newValue)
		sender.success(
			"Updated ${planet.name} rogue to $newValue from $oldValue"
		)
		planet.setLocation(true)
		spaceWorld.save()
		SpaceMap.refresh()
	}

	@Suppress("Unused")
	@Subcommand("set orbitpos")
	@CommandCompletion("@planets position")
	fun onSetOrbitPos(sender: CommandSender, planet: CachedPlanet, newValue: Double) {
		val oldValue = planet.orbitProgress
		val spaceWorld = planet.spaceWorld ?: throw InvalidCommandArgument("That planet's space world isn't loaded!")

		planet.setOrbitProgress(newValue)
		sender.success(
			"Updated ${planet.name} orbit progress from $oldValue to $newValue"
		)
		planet.orbit(true)
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
		sender.success("Moved ${planet.name} to $x, $z")
		planet.setLocation(updateDb = true)
		spaceWorld.save()
		SpaceMap.refresh()
	}

	@Suppress("Unused")
	@Subcommand("set orbit distance")
	@CommandCompletion("@planets @nothing")
	fun onSetOrbitDistance(sender: CommandSender, planet: CachedPlanet, newDistance: Int) {
		val oldDistance = planet.orbitDistance
		planet.changeOrbitDistance(newDistance)
		sender.success(
			"Updated distance from $oldDistance to $newDistance, moved the planet, and updated database"
		)
	}

	@Suppress("Unused")
	@Subcommand("set description")
	@CommandCompletion("@planets @nothing")
	fun onSetDescription(sender: CommandSender, planet: CachedPlanet, newDescription: String) {
		val oldDescription = planet.description
		planet.changeDescription(newDescription)
		SpaceMap.refresh()

		sender.success("Updated description from $oldDescription to $newDescription, and updated the space map.")
	}

	@Suppress("Unused")
	@Subcommand("getpos")
	@CommandCompletion("@planets")
	fun onGetPos(sender: CommandSender, planet: CachedPlanet) {
		sender.information(
			"${planet.name} is at ${planet.location} in ${planet.spaceWorldName}. " +
				"Its planet world is ${planet.planetWorldName}"
		)
	}

	@Suppress("Unused")
	@Subcommand("info")
	@CommandCompletion("@planets")
	fun onInfo(sender: CommandSender, planet: CachedPlanet) {
		sender.sendRichMessage(
			"<dark_green>${planet.name}\n" +
				"  <gray>Sun: <aqua>${planet.sun.name}\n" +
				"  <gray>Space World: <aqua>${planet.spaceWorldName}\n" +
				"  <gray>Planet World: <aqua>${planet.planetWorldName}\n" +
				"  <gray>Rogue: <aqua>${planet.rogue}\n" +
				"  <gray>Fixed location: <aqua>${planet.x}, ${planet.z}\n" +
				"  <gray>Size: <aqua>${planet.size}\n" +
				"  <gray>Atmosphere Density: <aqua>${planet.cloudDensity}\n" +
				"  <gray>Atmosphere Radius: <aqua>${planet.atmosphereRadius}\n" +
				"  <gray>Atmosphere Materials: <aqua>${planet.cloudMaterials}\n" +
				"  <gray>Crust Radius: <aqua>${planet.crustRadius}\n" +
				"  <gray>Crust Materials: <aqua>${planet.crustMaterials}" +
				"  <gray>Description: <aqua>${planet.description}"
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

		sender.success(
			"Orbited ${planet.name} in ${elapsedMilliseconds}ms"
		)
	}

	@Suppress("Unused")
	@Subcommand("orbit all")
	fun onOrbitAll(sender: CommandSender) {
		val elapsedNanos = measureNanoTime {
			Orbits.orbitPlanets()
		}

		val elapsedMilliseconds = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)
		sender.success("Orbited everything in ${elapsedMilliseconds}ms")
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

		sender.success(
			"Generated ${planet.name} in ${elapsedMilliseconds}ms"
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

		sender.success("Teleported to ${planet.name}")
	}

	@Subcommand("delete")
	@CommandCompletion("@planets")
	fun onDelete(sender: CommandSender, planet: CachedPlanet) {
		planet.erase()
		Planet.delete(planet.databaseId)
		Space.reload()
		sender.success("Deleted planet ${planet.name}")
	}
}
