package net.starlegacy.feature.hyperspace

import net.horizonsend.ion.server.IonServer
import net.starlegacy.SLComponent
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerSet

object HyperspaceBeacons : SLComponent() {
	fun reloadDynmap() {
		val api = try { DynmapPlugin.plugin.markerAPI } catch (_: Error) { return } // dynmap not installed
		var set: MarkerSet? = api.getMarkerSet("starships.hyperspace")

		set?.deleteMarkerSet()

		set = api.createMarkerSet("starships.hyperspace", "Hyperspace", null, false)

		for (beacon in IonServer.configuration.beacons) {
			val split = beacon.name.split("_")

			if (split.size != 2) continue

			val name =
				"${split[0].replaceFirstChar { it.uppercase() }} System -> ${split[1].replaceFirstChar { it.uppercase() }} System"

			val x = beacon.spaceLocation.x.toDouble()
			val z = beacon.spaceLocation.z.toDouble()
			set.createMarker(
				beacon.name,
				name,
				beacon.spaceLocation.world,
				x,
				128.0,
				z,
				api.getMarkerIcon("portal"),
				false
			)
		}
	}
}
