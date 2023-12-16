package net.horizonsend.ion.server.features.space

import net.horizonsend.ion.common.utils.text.createHtmlLink
import net.horizonsend.ion.common.utils.text.wrapStyle
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.Color
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerSet
import kotlin.random.Random

object SpaceMap : IonServerComponent(true) {
	private lateinit var markerSet: MarkerSet

	override fun onEnable() {
		if (!getPluginManager().isPluginEnabled("dynmap")) {
			log.warn("Dynmap not enabled! Space map will not be enabled.")
			return
		}

		Tasks.syncDelay(20) {
			refresh()
		}
	}

	fun refresh() = Tasks.sync {
		val markerAPI = DynmapPlugin.plugin.markerAPI

		markerAPI.getMarkerSet("space")?.deleteMarkerSet()
		markerSet = markerAPI.createMarkerSet("space", "Space", null, false)

		for (star in Space.getStars()) {
			if (star.name == "EdenHack") continue

			markerSet.createMarker(
				star.id,
				star.name,
				star.spaceWorldName,
				star.location.x.toDouble(),
				star.location.y.toDouble(),
				star.location.z.toDouble(),
				markerAPI.getMarkerIcon("sun"),
				false // ??
			)
		}

		for (planet in Space.getPlanets()) {
			val serverName = IonServer.configuration.serverName
			val link = "https://$serverName.horizonsend.net/?worldname=${planet.planetWorldName}"

			val planetDescription = "${planet.name} \n \n ${planet.description}"

			// planet icon
			val planetMarker = markerSet.createMarker(
				planet.id, // Marker ID
				wrapStyle(createHtmlLink(planet.name, link, "#FFFFFF"), "h3", "font-size:30"), // Markup icon name
				true, // use HTML markup
				planet.spaceWorldName, // World
				planet.location.x.toDouble(), // x
				planet.location.y.toDouble(), // y
				planet.location.z.toDouble(), // z
				markerAPI.getMarkerIcon(planet.name.lowercase()), // Icon
				false // not persistent
			)

			planetMarker.description = planetDescription

			// planet ring
			markerSet.createCircleMarker(
				"${planet.id}_orbit",
				planet.name,
				false, // Allow html markup in icon labels
				planet.spaceWorldName,
				planet.sun.location.x.toDouble(),
				planet.sun.location.y.toDouble(),
				planet.sun.location.z.toDouble(),
				planet.orbitDistance.toDouble(),
				planet.orbitDistance.toDouble(),
				false // persistent
			)?.run {
				setFillStyle(0.0, 0) // make the inside empty

				val random = Random(planet.name.hashCode())
				val r = random.nextInt(128, 255)
				val g = random.nextInt(1, 20)
				val b = random.nextInt(128, 255)
				val color = Color.fromRGB(r, g, b)
				setLineStyle(lineWeight, lineOpacity, color.asRGB())
			}

			// Create a marker to escape the planet view
			val escapeLink = "https://$serverName.horizonsend.net/?worldname=${planet.spaceWorldName}"

			markerSet.createMarker(
				"${planet.id}_escape",
				wrapStyle(createHtmlLink("View Space", escapeLink, "#FFFFFF"), "h3", "font-size:50"),
				true,
				planet.planetWorldName,
				-100.0,
				384.0,
				-100.0,
				markerAPI.getMarkerIcon("world"),
				false
			)
		}
	}

}
