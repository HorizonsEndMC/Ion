package net.horizonsend.ion.server.features.starship.hyperspace

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerSet

object HyperspaceBeacons : IonServerComponent(true) {
	override fun onEnable() {
		reloadDynmap()
	}

	fun reloadDynmap() {
		val api = try { DynmapPlugin.plugin.markerAPI } catch (_: Error) { return } // dynmap not installed
		var set: MarkerSet? = api.getMarkerSet("beacons")

		set?.deleteMarkerSet()

		set = api.createMarkerSet("beacons", "Beacons", null, false)

		for (beacon in IonServer.configuration.beacons) {
			val marker = set.createMarker(
				beacon.name,
				beacon.name,
				beacon.spaceLocation.world,
				beacon.spaceLocation.x.toDouble(),
				128.0,
				beacon.spaceLocation.z.toDouble(),
				api.getMarkerIcon("portal"),
				false
			)

			val serverName = IonServer.configuration.serverName
			val link = "https://$serverName.horizonsend.net/?worldname=${beacon.destination.world}"

			marker.description = """
				<h3><a href="$link">Open Destination Space Map</a></h3>
			""".trimIndent()
		}
	}
}
