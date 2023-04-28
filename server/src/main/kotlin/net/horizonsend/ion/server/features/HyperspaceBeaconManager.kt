package net.horizonsend.ion.server.features

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.event.StarshipTranslateEvent
import net.starlegacy.feature.starship.event.StarshipUnpilotedEvent
import net.starlegacy.util.isInRange
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID

object HyperspaceBeaconManager : Listener {
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

	@EventHandler
	fun onStarshipUnpilot(event: StarshipUnpilotedEvent) {
		activeRequests.remove(event.player.uniqueId)
	}

	@EventHandler
	fun onStarshipMove(event: StarshipTranslateEvent) {
		clearExpired()
		val pilot = event.starship.pilot ?: return
		if (event.starship.hyperdrives.isEmpty()) return

		val starship = event.starship
		val worldBeacons = beaconWorlds[event.starship.serverLevel.world] ?: return

		if (
			worldBeacons.any { beacon ->
				if (beacon.spaceLocation.toLocation().isInRange(
						Location(
								event.starship.serverLevel.world,
								(event.x + starship.centerOfMass.x).toDouble(),
								(event.y + starship.centerOfMass.y).toDouble(),
								(event.z + starship.centerOfMass.x).toDouble()
							),
						beacon.radius
					)
				) {
					event.starship.beacon = beacon
					true
				} else {
					event.starship.beacon = null
					false
				}
			}
		) {
			if (activeRequests.containsKey(pilot.uniqueId)) return
			val beacon = event.starship.beacon

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
