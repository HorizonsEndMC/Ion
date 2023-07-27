package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.event.StarshipExitHyperspaceEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotedEvent
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.distance
import org.bukkit.event.EventHandler
import java.util.UUID

object HyperspaceBeaconManager : SLEventListener() {
	// Your problem if it throws null pointers
	val beaconWorlds get() = IonServer.configuration.beacons.groupBy { it.spaceLocation.bukkitWorld() }

	// Make it yell at you once every couple seconds not every time your ship moves
	private val activeRequests: MutableMap<UUID, Long> = mutableMapOf()

	private fun clearExpired() {
		activeRequests.filterValues {
			it + 1000 * 30 < System.currentTimeMillis()
		}.keys.forEach {
			activeRequests.remove(it)
		}
	}

	@Suppress("unused")
	@EventHandler
	fun onStarshipUnpilot(event: StarshipUnpilotedEvent) {
		activeRequests.remove(event.player.uniqueId)
	}

	@Suppress("unused")
	@EventHandler
	fun onStarshipMove(event: StarshipTranslateEvent) {
		clearExpired()
		detectNearbyBeacons(event.starship, event.x, event.z)
	}

	@Suppress("unused")
	@EventHandler
	fun onStarshipExitHyperspace(event: StarshipExitHyperspaceEvent) {
		if (event.starship is ActivePlayerStarship) {
			detectNearbyBeacons(event.starship, 0, 0)
		}
	}

	private fun detectNearbyBeacons(starship: ActivePlayerStarship, x: Int, z: Int) {
		val pilot = starship.pilot ?: return
		if (starship.hyperdrives.isEmpty()) return

		val worldBeacons = beaconWorlds[starship.serverLevel.world] ?: return

		if (
			worldBeacons.any { beacon ->
				val distance = distance(
					beacon.spaceLocation.x,
					beacon.spaceLocation.z,
					(x + starship.centerOfMass.x),
					(z + starship.centerOfMass.z)
				)

				if (distance <= beacon.radius) {
					starship.beacon = beacon
					true
				} else {
					starship.beacon = null
					false
				}
			}
		) {
			if (activeRequests.containsKey(pilot.uniqueId)) return
			val beacon = starship.beacon

			if (beacon?.prompt != null) pilot.sendRichMessage(beacon.prompt)
			pilot.sendRichMessage(
				"<aqua>Detected signal from hyperspace beacon<yellow> ${beacon!!.name}<aqua>" + // not null if true
						", destination<yellow> " +
						"${beacon.destinationName ?: "${beacon.destination.world}: ${beacon.destination.x}, ${beacon.destination.z}"}<aqua>. " +
						"<gold><italic><hover:show_text:'<gray>/usebeacon'><click:run_command:/usebeacon>Engage hyperdrive?</click>"
			)
			activeRequests[pilot.uniqueId] = System.currentTimeMillis()
		} else {
			if (activeRequests.containsKey(pilot.uniqueId)) {
				if (!activeRequests.containsKey(pilot.uniqueId)) return // returned already if null

				pilot.information("Exited beacon communication radius.")
				activeRequests.remove(pilot.uniqueId)
				return
			}
			return
		}
	}
}
