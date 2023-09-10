package net.horizonsend.ion.server.features.starship.active.ai

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipComputers
import net.horizonsend.ion.server.features.starship.StarshipDealers.resolveTarget
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler

object AIUtils : IonServerComponent() {
	@EventHandler
	fun onAIUnpilot(event: StarshipUnpilotEvent) {
		val starship = event.starship

		if (starship.controller !is AIController && !starship.isExploding) return

		StarshipDestruction.vanish(starship)
	}

	fun createFromClipboard(
		location: Location,
		schematic: Clipboard,
		type: StarshipType,
		starshipName: String,
		createController: (ActiveStarship) -> Controller,
		callback: (ActiveControlledStarship) -> Unit = {}
	) {
		val target = resolveTarget(schematic, location)
		val vec3i = Vec3i(target)

		placeSchematicEfficiently(schematic, location.world, vec3i, true) {
			tryPilotWithController(location.world, vec3i, type, starshipName, createController) {
				callback(it)
			}
		}
	}

	private fun tryPilotWithController(
		world: World,
		origin: Vec3i,
		type: StarshipType,
		name: String,
		createController: (ActiveStarship) -> Controller,
		callback: (ActiveControlledStarship) -> Unit = {}
	) {
		val block = world.getBlockAtKey(origin.toBlockKey())

		if (block.type != StarshipComputers.COMPUTER_TYPE) {
			warnDetectionFailure("${block.type} at $origin was not a starship computer, failed to pilot", origin)
			return
		}

		DeactivatedPlayerStarships.createAsync(block.world, block.x, block.y, block.z, name) { data ->
			Tasks.async {
				try {
					DeactivatedPlayerStarships.updateType(data, type)
					val state = StarshipDetection.detectNewState(data)

					DeactivatedPlayerStarships.updateState(data, state)

					Tasks.sync { PilotedStarships.activateWithoutPilot(data, createController, callback) }
				} catch (e: StarshipDetection.DetectionFailedException) {
					warnDetectionFailure("Detection failed: ${e.message}", origin)
				}
			}
		}
	}

	fun warnDetectionFailure(reason: String, computerLoc: Long) = warnDetectionFailure(reason, Vec3i(computerLoc))

	fun warnDetectionFailure(reason: String, computerLoc: Vec3i) =
		IonServer.slF4JLogger.warn("Could not activate AI ship at ${computerLoc}! " + reason)
}
