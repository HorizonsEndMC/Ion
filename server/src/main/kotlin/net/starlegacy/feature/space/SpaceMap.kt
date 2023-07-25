package net.starlegacy.feature.space

import net.horizonsend.ion.server.IonServerComponent
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.Color
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerSet
import kotlin.random.Random

object SpaceMap : IonServerComponent(true) {
	private lateinit var markerSet: MarkerSet

	override fun onEnable() {
		if (!getPluginManager().isPluginEnabled("dynmap")) return

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
			// planet icon
			markerSet.createMarker(
				planet.id,
				planet.name,
				planet.spaceWorldName,
				planet.location.x.toDouble(),
				planet.location.y.toDouble(),
				planet.location.z.toDouble(),
				markerAPI.getMarkerIcon(planet.name.lowercase()),
				false // ??
			)

			// planet ring
			markerSet.createCircleMarker(
				"${planet.id}_orbit",
				planet.name,
				false, // ??
				planet.spaceWorldName,
				planet.sun.location.x.toDouble(),
				planet.sun.location.y.toDouble(),
				planet.sun.location.z.toDouble(),
				planet.orbitDistance.toDouble(),
				planet.orbitDistance.toDouble(),
				false // ??
			)?.run {
				setFillStyle(0.0, 0) // make the inside empty

				val random = Random(planet.name.hashCode())
				val r = random.nextInt(128, 255)
				val g = random.nextInt(1, 20)
				val b = random.nextInt(128, 255)
				val color = Color.fromRGB(r, g, b)
				setLineStyle(lineWeight, lineOpacity, color.asRGB())
			}
		}
	}

	override fun vanillaOnly(): Boolean {
		return true
	}
}
