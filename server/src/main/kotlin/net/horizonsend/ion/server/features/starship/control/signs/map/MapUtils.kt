package net.horizonsend.ion.server.features.starship.control.signs.map

import net.horizonsend.ion.common.database.cache.BookmarkCache
import net.horizonsend.ion.common.database.schema.misc.Bookmark
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.CelestialBody
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Vector3d
import org.joml.Vector3f
import kotlin.math.pow

//All of these are stored under the font Special
enum class MapTextIcon(val char: Char) {
	BORDER_RIGHT_MISSING('\ueBF1'), //ascent 6, height 8
	ONE_PIXEL('\ueBF2'), //ascent 0, height 1
	CIRCLE_HOLLOW('\ueBF3'), //ascent 7, height 8
	CIRCLE_FILLED_TRANSPARENT('\ueBF4'); //ascent 7, height 8
	fun component(): Component {
		return Component.text(char).font(SPECIAL_FONT_KEY)
	}
}

fun getSidebarKeyToUse(ship: Starship) = when(ship.forward){
	BlockFace.NORTH -> Sidebar.fontKey
	BlockFace.EAST -> Sidebar.fontKey90
	BlockFace.SOUTH -> Sidebar.fontKey180
	BlockFace.WEST -> Sidebar.fontKey270

	else -> {
		Sidebar.fontKey
	}
}

fun shipScale(displayMap: DisplayMap) = when (displayMap.maxDistance) {
	1000.0 -> .05
	2000.0 -> .035
	3000.0 -> .025
	else -> .02
}

fun celestialBodyLocalMapScale(body: CelestialBody, displayMap: DisplayMap) = when (body) {
	is CachedPlanet -> body.size*(360.0/(displayMap.maxDistance.pow(.95)))
	is CachedStar -> (4.0*body.outerSphereRadius.d())/(displayMap.maxDistance.pow(.95))
	else -> {1.0*(160.0/displayMap.maxDistance)}
}

fun shipsInRange(maxDistance: Double, sourceShip: Starship): List<Starship> {
	return (if (sourceShip.playerPilot != null) {
		(Fleets.findByMember(sourceShip.playerPilot!!)?.getJointContacts() ?: sourceShip.getContacts())
			.filter { it.centerOfMass.distance(sourceShip.centerOfMass) < maxDistance/2.0 }
	} else sourceShip.getContacts()).filter { it.centerOfMass.distance(sourceShip.centerOfMass) < maxDistance/2.0 }
}

fun celestialBodiesInRange(maxDistance: Double, source: Vector, world: World) : List<CelestialBody>{
	return Space.getAllCelestialBodies().filter {
		it.spaceWorld == world && it.location.toVector().distance(source) <= maxDistance/2.0
	}
}

fun starsInRange(maxDistance: Double, source: Vector, world: World) : List<CachedStar>{
	return Space.getStars().filter {
		it.spaceWorld == world && it.location.toVector().distance(source) <= maxDistance/2.0
	}
}

fun planetInRange(maxDistance: Double, source: Vector, world: World) : List<CachedPlanet> {
	return Space.getAllPlanets().filter {
			it.spaceWorld == world && it.location.toVector()
				.distance(source) <= maxDistance/2.0
	}
}

fun beaconsInRange(maxDistance: Double, centerOfMass: Vector, world: World): List<ServerConfiguration.HyperspaceBeacon> {
	return ConfigurationFiles.serverConfiguration().beacons.filter {
		it.spaceLocation.bukkitWorld() == world &&
			it.spaceLocation.toLocation().toVector()
				.distance(centerOfMass) <= maxDistance/2.0
	}
}

fun bookmarksInRange(displayMap: DisplayMap, maxDistance: Double, centerOfMass: Vector, world: World): List<Bookmark> {
	val player = displayMap.ship.playerPilot ?: return listOf()
	return BookmarkCache.getAll().filter { bm -> bm.owner == player.slPlayerId }.filter {
		it.worldName == world.name &&
			Vector(it.x, it.y, it.z).distance(centerOfMass) <= maxDistance/2.0
	}
}

fun Bookmark.toVector() = Vector(x.toDouble(), y.toDouble(), z.toDouble())

fun Transformation.clone(): Transformation =
	Transformation(this.translation, this.leftRotation, this.scale, this.rightRotation)

fun Vector3d.toVector3f() = Vector3f(this.x().toFloat(), this.y().toFloat(), this.z().toFloat())

fun saveStateToLocation(location: Location, mapState: MapState, size: Double) : Boolean{
	try {
		val block = location.world.getBlockAt(location)
		val state = block.state as? Sign ?: return false
		val pdc = state.persistentDataContainer
		pdc.set(NamespacedKeys.MAP_STATE, PersistentDataType.STRING, mapState.name)
		pdc.set(NamespacedKeys.MAP_SIZE, PersistentDataType.DOUBLE, size)
		return state.update()
	}catch (_: Exception){
	}

	return false
}

fun loadStateFromLocation(location: Location): Triple<Boolean, MapState, Double>{
	try {
		val block = location.world.getBlockAt(location)
		val state = block.state as? Sign ?: return Triple(false,MapState.LOCAL_MAP, 1.0)
		val pdc = state.persistentDataContainer
		val mapState = pdc.get(NamespacedKeys.MAP_STATE, PersistentDataType.STRING)
		val size = pdc.get(NamespacedKeys.MAP_SIZE, PersistentDataType.DOUBLE)
		if (mapState ==null || size == null) return Triple(false,MapState.LOCAL_MAP, 1.0)
		val enumMapState = MapState.valueOf(mapState)
		return Triple(true, enumMapState, size)
	}catch (_: Exception){
	}
	return Triple(false,MapState.LOCAL_MAP, 1.0)
}
