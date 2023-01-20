package net.horizonsend.ion.server.managers

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.feature.starship.event.StarshipTranslateEvent
import net.starlegacy.feature.starship.event.StarshipUnpilotedEvent
import net.starlegacy.util.isInRange
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerSet
import java.util.UUID
import kotlin.time.measureTime

object HyperspaceBeaconManager : Listener {
	private val beaconWorlds get() = Ion.configuration.beacons.associateBy { it.spaceLocation.bukkitWorld() } // Your problem if it throws null pointers

	// Make it yell at you once every couple seconds not every time your ship moves
	private val activeRequests: MutableMap<UUID, Long> = mutableMapOf()

	private fun addRequest(uuid: UUID) {
		clearExpired()
		activeRequests[uuid] = System.currentTimeMillis()
	}

	private fun clearExpired() {
		activeRequests.filterValues {
			it + 1000 * 30 < System.currentTimeMillis()
		}.keys.forEach {
			activeRequests.remove(it)
		}
	}

	@EventHandler
	fun onStarshipUnpilot(event: StarshipUnpilotedEvent) {
		activeRequests.remove(event.player.uniqueId)
	}

	@EventHandler
	fun onStarshipMove(event: StarshipTranslateEvent) {
		if (event.starship.pilot == null) return
		if (event.starship.hyperdrives.isEmpty()) return

		val starship = event.starship
		val worldBeacons = beaconWorlds.filter { it.key == starship.serverLevel.world }

		if (!beaconWorlds.contains(event.movement.newWorld) ||
			!beaconWorlds.contains(event.starship.serverLevel.world)
		) {
			return
		}

		if (
			worldBeacons.any {
				if (it.value.spaceLocation.toLocation().isInRange(
						Location(event.movement.newWorld, event.x.toDouble(), event.y.toDouble(), event.z.toDouble()),
						it.value.radius
					)
				) {
					event.starship.beacon = it.value
					true
				} else {
					event.starship.beacon = null
					false
				}
			}
		) {
			val beacon = event.starship.beacon

			event.starship.pilot!!.sendRichMessage(
				"<aqua>Detected signal from hyperspace beacon<yellow> ${beacon!!.name}<aqua>" + // not null if true
					", destination<yellow> " +
					"${beacon.destinationName ?: "${beacon.destination.world}: ${beacon.destination.x}, ${beacon.destination.z}"}<aqua>. " +
					"<gold><italic><click:run_command:usebeacon>Engage hyperdrive?"
			)

			addRequest(event.starship.pilot!!.uniqueId)
		} else {
			if (!activeRequests.containsKey(event.starship.pilot!!.uniqueId)) return // returned already if null

			event.starship.pilot!!.sendFeedbackMessage(FeedbackType.INFORMATION, "Exited beacon communication radius.")
			activeRequests.remove(event.starship.pilot!!.uniqueId)
			return
		}
	}

	fun reloadDynmap() {
		val api = try { DynmapPlugin.plugin.markerAPI } catch (_: Error) { return } // dynmap not installed
		var set: MarkerSet? = api.getMarkerSet("starships.hyperspace")

		set?.deleteMarkerSet()

		set = api.createMarkerSet("starships.hyperspace", "Hyperspace", null, false)

		for (beacon in Ion.configuration.beacons) {
			val split = beacon.name.split("_")

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
