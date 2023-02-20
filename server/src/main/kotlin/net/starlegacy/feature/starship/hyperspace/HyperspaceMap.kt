package net.starlegacy.feature.starship.hyperspace

import net.starlegacy.SLComponent
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerSet

object HyperspaceMap : SLComponent() {

	private lateinit var markerSet: MarkerSet
	private var markers =  mutableMapOf<ActivePlayerStarship,HyperspaceMarker>()

	private val markerAPI: MarkerAPI get() = DynmapPlugin.plugin.markerAPI

	/** Draw the hyperspace markers on the dynmap
	 * TODO: consider moving the markers to the space set instead of own set*/
	override fun onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("dynmap")) return

		markerSet = markerAPI.createMarkerSet("hyperspace", "Hyperspace", null, false)
		Tasks.syncDelay(20) {
			refresh()
		}
	}
	/** Synchronous refresh of hyperspace markers*/
	fun refresh() = Tasks.sync {
		for (marker in markers.values) {
			marker.tick()
			drawMarker(marker)
		}
	}
	/** Adds a new marker to the collection*/
	fun addMarker(marker: HyperspaceMarker) {}

	fun deleteMarker(marker: HyperspaceMarker) {
		deleteDraw(marker, true, true)
	}

	/** Deletes the drawn components from the dynmap*/
	private fun deleteDraw(marker: HyperspaceMarker, delArrow: Boolean = false, delTracker: Boolean = false) {}

	/** Draws the marker on the Dynmap*/
	private fun drawMarker(marker: HyperspaceMarker) {
		if (marker.isArrow) {
			drawArrow(marker)
		} else {
			deleteDraw(marker, true)
		}
		if (marker.inHyperspace) {
			drawShipTrack(marker)
		}
	}

	private fun drawArrow(marker: HyperspaceMarker) {}

	private fun drawShipTrack(marker: HyperspaceMarker) {}

}
