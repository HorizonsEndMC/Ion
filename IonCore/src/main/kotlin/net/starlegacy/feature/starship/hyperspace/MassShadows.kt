package net.starlegacy.feature.starship.hyperspace

import kotlin.math.sqrt
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.squared
import org.bukkit.World

object MassShadows {
	private const val PLANET_RADIUS = 1000
	private const val STAR_RADIUS = 1800

	data class MassShadowInfo(val description: String, val x: Int, val z: Int, val radius: Int, val distance: Int)

	fun find(world: World, x: Double, z: Double): MassShadowInfo? {
		val realWorld = (if (Hyperspace.isHyperspaceWorld(world)) Hyperspace.getRealspaceWorld(world) else world)
			?: return null

		for (planet in Space.getPlanets()) {
			if (planet.spaceWorld != realWorld) {
				continue
			}
			val loc = planet.location
			var dist = distanceSquared(x, 128.0, z, loc.x.toDouble(), 128.0, loc.z.toDouble())
			if (dist > PLANET_RADIUS.squared()) {
				continue
			}
			dist = sqrt(dist)
			return MassShadowInfo("Planet ${planet.name}", loc.x, loc.z, PLANET_RADIUS, dist.toInt())
		}

		for (star in Space.getStars()) {
			if (star.spaceWorld != realWorld) {
				continue
			}
			val loc = star.location
			var dist = distanceSquared(x, 128.0, z, loc.x.toDouble(), 128.0, loc.z.toDouble())
			if (dist > STAR_RADIUS.squared()) {
				continue
			}
			dist = sqrt(dist)
			return MassShadowInfo("Star ${star.name}", loc.x, loc.z, STAR_RADIUS, dist.toInt())
		}

		for (otherShip in ActiveStarships.getInWorld(realWorld)) {
			if (!otherShip.isInterdicting) {
				continue
			}

			val otherX = otherShip.centerOfMass.x
			val otherY = otherShip.centerOfMass.y
			val otherZ = otherShip.centerOfMass.z
			var dist = distanceSquared(x, 128.0, z, otherX.toDouble(), otherY.toDouble(), otherZ.toDouble())
			if (dist > otherShip.interdictionRange.squared()) {
				continue
			}
			dist = sqrt(dist)
			return MassShadowInfo("Anomaly", otherX, otherY, otherZ, dist.toInt())
		}

		return null
	}
}
