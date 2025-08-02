package net.horizonsend.ion.server.features.starship.hyperspace

import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawners
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.LocusScheduler
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.PilotedStarships.getDisplayName
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distance
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.World
import kotlin.math.roundToInt
import kotlin.math.sqrt

object MassShadows {
	const val PLANET_RADIUS = 1000
	const val STAR_RADIUS = 1800

	data class MassShadowInfo(val description: Component, val x: Int, val z: Int, val radius: Int, val distance: Int, val wellStrength: Int)

	fun find(world: World, x: Double, z: Double): List<MassShadowInfo>? {
		val allMassShadows = mutableListOf<MassShadowInfo>()
		val realWorld = (if (Hyperspace.isHyperspaceWorld(world)) Hyperspace.getRealspaceWorld(world) else world)
			?: return null

		for (planet in Space.getAllPlanets()) {
			if (planet.spaceWorld != realWorld) continue

			val loc = planet.location
			var dist = distanceSquared(x, 128.0, z, loc.x.toDouble(), 128.0, loc.z.toDouble())

			if (dist > PLANET_RADIUS.squared()) continue

			dist = sqrt(dist)
			allMassShadows.add(MassShadowInfo(text("Planet ${planet.name}"), loc.x, loc.z, PLANET_RADIUS, dist.toInt(), 1000000))
		}

		for (star in Space.getStars()) {
			if (star.spaceWorld != realWorld) continue

			val loc = star.location
			var dist = distanceSquared(x, 128.0, z, loc.x.toDouble(), 128.0, loc.z.toDouble())

			if (dist > STAR_RADIUS.squared()) continue

			dist = sqrt(dist)

			allMassShadows.add(MassShadowInfo(text("Star ${star.name}"), loc.x, loc.z, STAR_RADIUS, dist.toInt(), 10000000))
		}

		for (otherShip in ActiveStarships.getInWorld(realWorld)) {
			if (!ActiveStarships.isActive(otherShip)) continue
			if (!otherShip.isInterdicting) continue

			val otherX = otherShip.centerOfMass.x
			val otherY = otherShip.centerOfMass.y
			val otherZ = otherShip.centerOfMass.z
			var dist = distanceSquared(x, 128.0, z, otherX.toDouble(), otherY.toDouble(), otherZ.toDouble())

			if (dist > Interdiction.starshipInterdictionRangeEquation(otherShip).squared()) continue

			dist = sqrt(dist)

			val interdictingShip = otherShip as? ActiveControlledStarship
			val interdictingShipName = interdictingShip?.data?.let { getDisplayName(it) } ?: otherShip.type.displayNameComponent

			allMassShadows.add(
				MassShadowInfo(
					text()
						.append(interdictingShipName)
						.append(text(" piloted by "))
						.append(interdictingShip?.controller?.pilotName ?: text("none"))
						.build(),
					otherX,
					otherZ,
					Interdiction.starshipInterdictionRangeEquation(otherShip).toInt(),
					dist.toInt(),
					otherShip.balancing.wellStrength.toInt()
				)
			)
		}

		for (locusScheduler in AISpawners.getAllSpawners().mapNotNull { it.scheduler as? LocusScheduler }) {
			if (!locusScheduler.active) continue
			val center = locusScheduler.center

			if (center.world.uid != realWorld.uid) continue

			val dist = distance(x, 128.0, z, center.x, 128.0, center.z)
			if (dist > locusScheduler.radius) continue

			allMassShadows.add(MassShadowInfo(text("AI Locus"), center.blockX, center.blockZ, locusScheduler.radius.roundToInt(), dist.toInt(), 1))
		}

		if (allMassShadows.isEmpty()) return null
		else return allMassShadows
	}
}
