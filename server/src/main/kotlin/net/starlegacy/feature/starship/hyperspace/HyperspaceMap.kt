package net.starlegacy.feature.starship.hyperspace

import net.starlegacy.SLComponent
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerSet

object HyperspaceMap : SLComponent() {

	private lateinit var markerSet: MarkerSet
	private var markers = mutableMapOf<ActiveStarship, HyperspaceMarker>()

	private val markerAPI: MarkerAPI get() = DynmapPlugin.plugin.markerAPI

	/** Draw the hyperspace markers on the dynmap*/
	override fun onEnable() {
		println("Starting to enable hyperspace markers")
		if (!Bukkit.getPluginManager().isPluginEnabled("dynmap")) return
		println("Hyperspacemap enabled")

		markerAPI.getMarkerSet("hyperspace")?.deleteMarkerSet()

		Tasks.syncRepeat(40, 20) {
			refresh()
		}
	}

	/** Synchronous refresh of hyperspace markers*/
	fun refresh() = Tasks.sync {
		// Its possible that the dymap hasnt created the markerset yet so this
		// just keeps trying until its created
		val spaceSet = markerAPI.getMarkerSet("space") ?: return@sync
		markerSet = spaceSet
		for (marker in markers.values) {
			marker.tick()
			drawMarker(marker)
		}
	}

	/** Adds a new marker to the collection*/
	fun addMarker(ship: ActiveStarship, marker: HyperspaceMarker) {
		markers[ship] = marker
	}

	/**Deletes the marker from the collection and removes drawn components*/
	fun deleteMarker(ship: ActiveStarship) {
		val marker = markers[ship]
		if (marker != null) {
			deleteDraw(marker, true, true)
			markers.remove(ship)
		}
	}

	fun getMarker(ship: ActiveStarship): HyperspaceMarker? { return markers[ship] }

	/** Deletes the drawn components from the dynmap*/
	private fun deleteDraw(marker: HyperspaceMarker, delArrow: Boolean = false, delTracker: Boolean = false) {
		if (delArrow) {
			var dynMarker = markerSet.findPolyLineMarker(marker.id.toString() + "arrowBody")
			dynMarker?.deleteMarker()
			dynMarker = markerSet.findPolyLineMarker(marker.id.toString() + "arrowHead")
			dynMarker?.deleteMarker()
		}
		if (delTracker) {
			var dynMarker = markerSet.findMarker(marker.id.toString() + "tracker")
			dynMarker?.deleteMarker()
		}
	}

	/** Draws the marker on the Dynmap*/
	private fun drawMarker(marker: HyperspaceMarker) {
		/*if (marker.isArrow) {
			drawArrow(marker)
		} else {
			deleteDraw(marker, true)
		}*/
		if (marker.inHyperspace) {
			drawShipTrack(marker)
		}
	}

	/** uses the Dynmap API to draw a spline arrow */
	private fun drawArrow(marker: HyperspaceMarker) {
		// Check if arrow already exists
		if (markerSet.findPolyLineMarker(marker.id.toString() + "arrowBody") != null) { return }
		val vectors = marker.arrowVects
		markerSet.createPolyLineMarker(
			marker.id.toString() + "arrowBody",
			"arrowBody",
			false,
			marker.org.world.name,
			doubleArrayOf(vectors[0].x, vectors[1].x),
			doubleArrayOf(vectors[0].y, vectors[1].y),
			doubleArrayOf(vectors[0].z, vectors[1].z),
			false
		)

		markerSet.createPolyLineMarker(
			marker.id.toString() + "arrowHead",
			"arrowHead",
			false,
			marker.org.world.name,
			doubleArrayOf(vectors[2].x, vectors[1].x, vectors[3].x),
			doubleArrayOf(vectors[2].y, vectors[1].y, vectors[3].y),
			doubleArrayOf(vectors[2].z, vectors[1].z, vectors[3].z),
			false
		)
	}

	/** uses the Dynmap API to draw a marker for a ship in hyperspace*/
	private fun drawShipTrack(marker: HyperspaceMarker) {
		var dynMarker = markerSet.findMarker(marker.id.toString() + "tracker")
		if (dynMarker == null) {
			dynMarker = markerSet.createMarker(
				marker.id.toString() + "tracker",
				marker.id.toString(),
				marker.org.world.name,
				0.0, 0.0, 0.0,
				markerAPI.getMarkerIcon("walk"),
				false
			)
		}
		dynMarker.setLocation(marker.org.world.name, marker.pos.x, marker.pos.y, marker.pos.z)
	}
}
