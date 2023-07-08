package net.starlegacy.feature.hyperspace

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonComponent
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerSet

object HyperspaceBeacons : IonComponent() {
	fun reloadDynmap() {
		val api = try { DynmapPlugin.plugin.markerAPI } catch (_: Error) { return } // dynmap not installed
		var set: MarkerSet? = api.getMarkerSet("beacons")

		set?.deleteMarkerSet()

		set = api.createMarkerSet("beacons", "Beacons", null, false)

		for (beacon in IonServer.configuration.beacons) {
			set.createMarker(
				beacon.name,
				beacon.name,
				beacon.spaceLocation.world,
				beacon.spaceLocation.x.toDouble(),
				128.0,
				beacon.spaceLocation.z.toDouble(),
				api.getMarkerIcon("portal"),
				false
			)
		}
	}
}
