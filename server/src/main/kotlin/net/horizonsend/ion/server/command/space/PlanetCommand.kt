package net.horizonsend.ion.server.command.space

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.space.Moon
import net.horizonsend.ion.common.database.schema.space.Planet
import net.horizonsend.ion.common.database.schema.space.RoguePlanet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.space.Orbits
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceMap
import net.horizonsend.ion.server.features.space.body.CachedMoon
import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.OrbitingCelestialBody
import net.horizonsend.ion.server.features.space.body.planet.CachedOrbitingPlanet
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.features.space.body.planet.CachedRoguePlanet
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.orNull
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

@CommandAlias("planet")
@CommandPermission("space.planet")
object PlanetCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(CachedPlanet::class.java) { c: BukkitCommandExecutionContext ->
			Space.planetNameCache[c.popFirstArg().uppercase(Locale.getDefault())].orNull()
				?: throw InvalidCommandArgument("No such planet")
		}

		registerAsyncCompletion(manager, "stars") { _ -> Space.getStars().map(CachedStar::name) }
		registerAsyncCompletion(manager, "planets") { _ -> Space.getAllPlanets().map(CachedPlanet::name) }
		registerAsyncCompletion(manager, "planetsInWorld") { e -> Space.getAllPlanets()
			.filter { planet -> planet.spaceWorldName == e.player.world.name }
			.map(CachedPlanet::name)
		}
	}

	@Subcommand("create normal")
	@CommandCompletion("@nothing @stars @worlds 1000|2000|3000|4000|5000 1|2|3|4|5 0.5|0.75|1.0")
	fun onCreateNormal(
		sender: CommandSender,
		name: String,
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

		val seed: Long = name.hashCode().toLong()
		val orbitProgress: Double = randomDouble(0.0, 360.0)

		Planet.create(
			name,
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

	@Subcommand("create rogue")
	@CommandCompletion("@nothing x y z @worlds 1000|2000|3000|4000|5000 1|2|3|4|5 0.5|0.75|1.0")
	fun onCreateRogue(
		sender: CommandSender,
		name: String,
		x: Int,
		y: Int,
		z: Int,
		world: World,
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

		val seed: Long = name.hashCode().toLong()

		if (!world.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
			throw InvalidCommandArgument("Not a space world!")
		}

		RoguePlanet.create(
			name,
			x,
			y,
			z,
			world.name,
			planetWorldName,
			size,
			seed
		)

		Space.reload()

		val planet: CachedPlanet = Space.planetNameCache[name].get()

		sender.success("Created planet $name at ${planet.location}")
	}

	@Subcommand("create moon")
	@CommandCompletion("@nothing @planets @worlds 1000|2000|3000|4000|5000 1|2|3|4|5 0.5|0.75|1.0")
	fun onCreateMoon(
		sender: CommandSender,
		name: String,
		parent: CachedPlanet,
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

		val seed: Long = name.hashCode().toLong()
		val orbitProgress: Double = randomDouble(0.0, 360.0)

		Moon.create(
			name,
			parent.databaseId,
			planetWorldName,
			size,
			orbitDistance,
			orbitSpeed,
			orbitProgress,
			seed
		)

		Space.reload()

		val planet: CachedPlanet = Space.planetNameCache[name].get()

		sender.success("Created moon $name at ${planet.location}")
	}

	@Suppress("Unused")
	@Subcommand("set seed")
	@CommandCompletion("@planets 0")
	fun onSetSeed(sender: CommandSender, planet: CachedPlanet, newSeed: Long) {
		planet.setSeed(newSeed)

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

		planet.setCloudMaterials(materials)

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
		planet.setCloudDensity(newDensity)

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
		planet.setCloudNoise(newNoise)

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
		planet.setCloudThreshold(newThreshold)

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
		planet.setCloudNoise(newNoise)

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
		planet.setCrustNoise(newNoise)

		val planetName = planet.name
		Space.reload()
		Space.planetNameCache[planetName].get().generate()

		sender.success("Updated crust noise in database, reloaded systems, and regenerated planet.")
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

		planet.setCrustMaterials(materials)

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
		failIf(planet !is CachedOrbitingPlanet) { "$planet doesn't orbit a star!" }
		planet as CachedOrbitingPlanet

		val oldSun = planet.sun
		planet.changeSun(newSun)
		sender.success(
			"Updated sun from ${oldSun.name} to ${newSun.name}, moved the planet, and updated database"
		)
	}

	@Suppress("Unused")
	@Subcommand("set sun")
	@CommandCompletion("@planets @planets")
	fun onSetParent(sender: CommandSender, planet: CachedPlanet, newSun: CachedPlanet) {
		failIf(planet !is CachedMoon) { "$planet doesn't orbit a star!" }
		planet as CachedMoon

		failIf(planet == newSun) { "A planet can't orbit itself!" }

		val oldSun = planet.parent
		planet.changeSun(newSun)
		sender.success(
			"Updated sun from ${oldSun.name} to ${newSun.name}, moved the planet, and updated database"
		)
	}

	@Suppress("Unused")
	@Subcommand("set orbitpos")
	@CommandCompletion("@planets position")
	fun onSetOrbitPos(sender: CommandSender, planet: CachedPlanet, newValue: Double) {
		failIf(planet !is OrbitingCelestialBody) { "Planet $planet doesn't orbit!" }
		planet as OrbitingCelestialBody

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
	fun onSetLocation(sender: CommandSender, planet: CachedPlanet, x: Int, y: Int, z: Int) {
		val spaceWorld = planet.spaceWorld ?: throw InvalidCommandArgument("That planet's space world isn't loaded!")
		failIf(planet !is CachedRoguePlanet) { "Planet isn't rogue!" }
		planet as CachedRoguePlanet

		planet.setLocation(Vec3i(x, y, z))
		sender.success("Moved ${planet.name} to $x, $z")
		spaceWorld.save()
		SpaceMap.refresh()
	}

	@Suppress("Unused")
	@Subcommand("set orbit distance")
	@CommandCompletion("@planets @nothing")
	fun onSetOrbitDistance(sender: CommandSender, planet: CachedPlanet, newDistance: Int) {
		failIf(planet !is OrbitingCelestialBody) { "Planet $planet doesn't orbit!" }
		planet as OrbitingCelestialBody

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
				"Its planet world is ${planet.enteredWorldName}"
		)
	}

	@Suppress("Unused")
	@Subcommand("info")
	@CommandCompletion("@planets")
	fun onInfo(sender: CommandSender, planet: CachedPlanet) {
		sender.sendMessage(planet.formatInformation())
	}

	@Suppress("Unused")
	@Subcommand("orbit")
	@CommandCompletion("@planets")
	fun onOrbit(sender: CommandSender, planet: CachedPlanet) {
		val spaceWorld = planet.spaceWorld ?: throw InvalidCommandArgument("That planet's space world isn't loaded!")
		failIf(planet !is OrbitingCelestialBody) { "Planet ${planet.name} can't orbit!" }

		val elapsedNanos = measureNanoTime {
			(planet as OrbitingCelestialBody).orbit(true)
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

		planet.delete()

		Space.reload()
		sender.success("Deleted planet ${planet.name}")
	}
}
