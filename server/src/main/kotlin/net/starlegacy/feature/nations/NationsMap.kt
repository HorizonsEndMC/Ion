package net.starlegacy.feature.nations

import net.horizonsend.ion.common.database.schema.nations.Nation
import net.starlegacy.SLComponent
import net.horizonsend.ion.server.features.cache.nations.NationCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.string
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionCapturableStation
import net.starlegacy.feature.nations.region.types.RegionSpaceStation
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.Color
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.AreaMarker
import org.dynmap.markers.CircleMarker
import org.dynmap.markers.Marker
import org.dynmap.markers.MarkerAPI
import java.io.Closeable

@Suppress("unused", "MemberVisibilityCanBePrivate")
object NationsMap : SLComponent() {
	private fun syncOnly(block: () -> Unit) = when {
		Bukkit.isPrimaryThread() -> block()
		else -> Tasks.sync(block)
	}

	private val dynmapLoaded by lazy { Bukkit.getPluginManager().isPluginEnabled("dynmap") }

	private val markerAPI: MarkerAPI get() = DynmapPlugin.plugin.markerAPI

	private val markerSet
		get() = markerAPI.getMarkerSet("nations")
			?: markerAPI.createMarkerSet("nations", "Nations, Settlements, & Stations", null, false)

	private lateinit var updates: Closeable

	override fun onEnable() {
		if (!dynmapLoaded) {
			log.warn("Dynmap not loaded!")
		}

		updates = Nation.watchUpdates { change ->
			change[Nation::name]?.let {
				updateOwners()
			}

			change[Nation::color]?.let {
				updateOwners()
			}
		}

		reloadDynmap()
	}

	override fun onDisable() {
		updates.close()
	}

	fun reloadDynmap() = syncOnly {
		if (!dynmapLoaded) {
			return@syncOnly
		}

		markerSet.layerPriority = 100

		markerSet.areaMarkers.forEach(AreaMarker::deleteMarker)
		markerSet.markers.forEach(Marker::deleteMarker)

		// map has to load before other components so do this a tick later
		Tasks.sync {
			Regions.getAllOf<RegionTerritory>().forEach(::addTerritory)
			Regions.getAllOf<RegionCapturableStation>().forEach(::addCapturableStation)
			Regions.getAllOf<RegionSpaceStation<*, *>>().forEach(::addSpaceStation)
		}
	}

	fun updateOwners() = syncOnly {
		if (!dynmapLoaded) {
			return@syncOnly
		}

		Regions.getAllOf<RegionTerritory>().forEach(NationsMap::updateTerritory)
		Regions.getAllOf<RegionCapturableStation>().forEach(NationsMap::updateCapturableStation)
		Regions.getAllOf<RegionSpaceStation<*, *>>().forEach(NationsMap::updateSpaceStation)
	}

	fun addTerritory(territory: RegionTerritory): Unit = syncOnly {
		if (!dynmapLoaded) {
			return@syncOnly
		}

		try {
			removeTerritory(territory)

			val world = territory.bukkitWorld ?: return@syncOnly
			val polygon = territory.polygon

			val xPoints = polygon.xpoints ?: error("Null x points for ${territory.name} in ${territory.world}")
			val yPoints = polygon.ypoints ?: error("Null y points for ${territory.name} in ${territory.world}")

			markerSet.createAreaMarker(
				territory.id.toString(),
				territory.name,
				false,
				world.name,
				xPoints.map { it.toDouble() }.toDoubleArray(),
				yPoints.map { it.toDouble() }.toDoubleArray(),
				false
			)

			updateTerritory(territory)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun removeTerritory(territory: RegionTerritory): Unit = syncOnly {
		markerSet.findAreaMarker(territory.id.toString())?.deleteMarker()
	}

	fun updateTerritory(territory: RegionTerritory): Unit = syncOnly {
		if (!dynmapLoaded) {
			return@syncOnly
		}

		val marker: AreaMarker? = markerSet.findAreaMarker(territory.id.toString())

		if (marker == null) {
			log.warn("No area marker for territory with ID ${territory.id}")
			addTerritory(territory)
			return@syncOnly
		}

		var fillOpacity = 0.2
		var fillRGB = Integer.parseInt("333333", 16)
		var lineThickness = 3
		var lineOpacity = 0.75
		var lineRGB = Integer.parseInt("ffffff", 16)

		val settlement: Settlement? = territory.settlement?.let(Settlement.Companion::findById)

		marker.label = territory.name

		if (settlement != null) {
			marker.label += " (${settlement.name})"

			val rgb: Int = Color.BLUE.asRGB()

			fillRGB = rgb
			lineOpacity = 0.5
			lineRGB = rgb
		}

		val npcOwner: NPCTerritoryOwner? = territory.npcOwner?.let(NPCTerritoryOwner.Companion::findById)

		if (npcOwner != null) {
			val name = npcOwner.name
			marker.label += " ($name)"
			val rgb = npcOwner.color
			fillOpacity = 0.3
			fillRGB = rgb
			lineOpacity = 0.75
			lineRGB = rgb
			lineThickness = 5
		}

		val nation: Nation? = settlement?.nation?.let(Nation.Companion::findById) ?: territory.nation?.let(Nation.Companion::findById)

		if (nation != null) {
			val rgb = nation.color
			fillOpacity = 0.2
			fillRGB = rgb
			lineOpacity = 0.5
			lineRGB = rgb
			marker.label += " (${nation.name})"
		}

		// special colors for settlement cities
		if (settlement?.cityState != null) {
			when (settlement.cityState) {
				Settlement.CityState.ACTIVE -> {
					// green when paid
					fillOpacity = 0.05
					fillRGB = Color.GREEN.asRGB()
				}
				// red when unpaid
				Settlement.CityState.UNPAID -> {
					fillOpacity = 0.5
					fillRGB = Color.RED.asRGB()
				}

				else -> {}
			}
			lineThickness = 5
			lineOpacity = 0.75
			lineRGB = fillRGB
		}

		if (settlement == null && nation == null && npcOwner == null) {
			marker.label += "\nCost: ${territory.cost}C"
		}

		marker.setFillStyle(fillOpacity, fillRGB)
		marker.setLineStyle(lineThickness, lineOpacity, lineRGB)
	}

	fun addCapturableStation(station: RegionCapturableStation): Unit = syncOnly {
		removeCapturableStation(station)

		val name = station.name
		val world = station.world
		val x = station.x
		val y = 128.0
		val z = station.z.toDouble()
		val radius = NATIONS_BALANCE.capturableStation.radius.toDouble()

		markerSet.createCircleMarker(name, name, false, world, x.toDouble(), y, z, radius, radius, false)

		updateCapturableStation(station)
	}

	fun removeCapturableStation(station: RegionCapturableStation) = syncOnly {
		if (!dynmapLoaded) {
			return@syncOnly
		}

		markerSet.findAreaMarker(station.name)?.deleteMarker()
	}

	fun updateCapturableStation(station: RegionCapturableStation): Unit = syncOnly {
		if (!dynmapLoaded) {
			return@syncOnly
		}

		val marker: CircleMarker = markerSet.findCircleMarker(station.name)
			?: return@syncOnly addCapturableStation(station)

		val nation = station.nation?.let(NationCache::get)

		val rgb = nation?.color ?: Color.WHITE.asRGB()
		marker.setFillStyle(0.0, Color.WHITE.asRGB())
		marker.setLineStyle(5, 0.8, rgb)

		val quarter = station.siegeTimeFrame

		marker.description = """
		<p><h2>${station.name}</h2></p><p>
		${if (nation == null) {
			""
		} else {
			"""
			<h3>Owned by ${nation.name}</h3>
			<p>Siege time during $quarter:00 (UTC)
			""".trimIndent()
		}}
		</p>
		""".trimIndent()
	}

	fun addSpaceStation(station: RegionSpaceStation<*, *>): Unit = syncOnly {
		if (!dynmapLoaded) {
			return@syncOnly
		}

		val id = getMarkerID(station)
		val label = station.name
		val markup = false // whether to use HTML for label
		val world = station.world
		val x = station.x.toDouble()
		val y = 128.0
		val z = station.z.toDouble()
		val xRadius = station.radius.toDouble()
		val zRadius = station.radius.toDouble()
		val persistent = false

		markerSet.findCircleMarker(id)?.deleteMarker()
		markerSet.createCircleMarker(id, label, markup, world, x, y, z, xRadius, zRadius, persistent)
		val marker: CircleMarker = markerSet.findCircleMarker(id)

		val rgb = station.color

		marker.setFillStyle(0.2, rgb)
		marker.setLineStyle(5, 0.4, rgb)

		marker.description = """
		<p><h2>${station.name}</h2></p>
 		<p><h3>Owned by ${station.ownerType} ${station.ownerName}</h3></p>
		<p><i>${station.radius} block radius</i></p>
		""".trimIndent()
	}

	fun removeSpaceStation(station: RegionSpaceStation<*, *>) = syncOnly {
		if (!dynmapLoaded) {
			return@syncOnly
		}

		markerSet.findCircleMarker(getMarkerID(station))?.deleteMarker()
	}

	fun updateSpaceStation(station: RegionSpaceStation<*, *>): Unit = syncOnly {
		if (!dynmapLoaded) {
			return@syncOnly
		}

		removeSpaceStation(station)
		addSpaceStation(station)
	}

	private fun getMarkerID(station: RegionSpaceStation<*, *>) =
		"nation-station-" + station.id.toString()

	override fun supportsVanilla(): Boolean {
		return true
	}
}
