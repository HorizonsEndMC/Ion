package net.horizonsend.ion.server.features.starship.hyperspace

import net.horizonsend.ion.common.utils.text.createHtmlLink
import net.horizonsend.ion.common.utils.text.wrapStyle
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.isDynmapEnabled
import net.horizonsend.ion.server.miscellaneous.utils.markerAPI
import net.horizonsend.ion.server.miscellaneous.utils.registerIcon
import org.dynmap.markers.MarkerSet

object HyperspaceBeacons : IonServerComponent(true) {
	override fun onEnable() {
		registerIcon("hyperspace_beacon")

		reloadDynmap()
	}

	fun reloadDynmap() {
		if (!isDynmapEnabled()) return

		var set: MarkerSet? = markerAPI.getMarkerSet("beacons")

		set?.deleteMarkerSet()

		set = markerAPI.createMarkerSet("beacons", "Beacons", null, false)

		for (beacon in IonServer.configuration.beacons) {
			val serverName = IonServer.configuration.serverName
			val link = "https://$serverName.horizonsend.net/?worldname=${beacon.destination.world}&x=${beacon.destination.x}&z=${beacon.destination.z}"

			set.createMarker(
				beacon.name,
				"""
					${wrapStyle(createHtmlLink(beacon.name, link, "#FFFFFF"), "h3", "font-size:50")}
					<p><b>Destination World: </b>${beacon.destination.world}</p>
					<p><b>Destination Location: ${beacon.destination.toVec3i()}</b></p>
					<p>${beacon.prompt ?: ""}</p>
				""".trimIndent(),
				true,
				beacon.spaceLocation.world,
				beacon.spaceLocation.x.toDouble(),
				128.0,
				beacon.spaceLocation.z.toDouble(),
				markerAPI.getMarkerIcon("hyperspace_beacon"),
				false
			)
		}
	}
}
