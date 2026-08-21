package net.horizonsend.ion.server.features.starship.control.signs.map

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.body.CachedMoon
import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.CelestialBody
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.kyori.adventure.text.Component
import org.bukkit.util.Vector

//All of these are stored under the font Special
enum class MapTextIcon(val char: Char) {
	BORDER_RIGHT_MISSING('\ueBF1'), //ascent 0, height 1
	ONE_PIXEL('\ueBF2'), //ascent 0, height 1
	CIRCLE_HOLLOW('\ueBF3'), //ascent 7, height 8
	CIRCLE_FILLED_TRANSPARENT('\ueBF4'); //ascent 7, height 8


	fun component(): Component {
		return Component.text(char).font(SPECIAL_FONT_KEY)
	}
}

fun celestialBodyScale(maxDistance: Double, body: CelestialBody) = when (body) {
	is CelestialBody -> maxDistance.toDouble()

}

fun shipScale(displayMap: DisplayMap) = when (displayMap.maxDistance) {
	1000.0 -> .05
	2000.0 -> .035
	3000.0 -> .025
	else -> .02
}

fun celestialBodyMapScale(body: CelestialBody, displayMap: DisplayMap) = when (body) {
	is CachedPlanet -> body.size*(150.0/displayMap.maxDistance)
	is CachedStar -> 2.0*body.outerSphereRadius.d()/displayMap.maxDistance
	else -> {1.0*(225.0/displayMap.maxDistance)}
}

fun shipsInRange(maxDistance: Double, sourceShip: Starship): List<Starship> {
	return (if (sourceShip.playerPilot != null) {
		Fleets.findByMember(sourceShip.playerPilot!!)?.getJointContacts() ?: sourceShip.getContacts()
	} else sourceShip.getContacts()).filter { it.centerOfMass.distance(sourceShip.centerOfMass) < maxDistance }
}

fun celestialBodiesInRange(displayMap: DisplayMap, maxDistance: Double, source: Vector) : List<CelestialBody>{
	return Space.getAllCelestialBodies().filter {
		it.spaceWorld == displayMap.location.world && it.location.toVector().distance(source) <= maxDistance
	}
}

fun starsInRange(displayMap: DisplayMap, maxDistance: Double, source: Vector) : List<CachedStar>{
	return Space.getStars().filter {
		it.spaceWorld == displayMap.location.world && it.location.toVector().distance(source) <= maxDistance
	}
}

fun planetInRange(displayMap: DisplayMap, maxDistance: Double, source: Vector) : List<CachedPlanet> {
	return Space.getAllPlanets().filter {
			it.spaceWorld == displayMap.location.world && it.location.toVector()
				.distance(source) <= maxDistance
	}
}

fun beaconsInRange(displayMap: DisplayMap, maxDistance: Double, centerOfMass: Vector): List<ServerConfiguration.HyperspaceBeacon> {
	return ConfigurationFiles.serverConfiguration().beacons.filter {
		it.spaceLocation.bukkitWorld() == displayMap.location.world &&
			it.spaceLocation.toLocation().toVector()
				.distance(centerOfMass) <= maxDistance
	}
}
