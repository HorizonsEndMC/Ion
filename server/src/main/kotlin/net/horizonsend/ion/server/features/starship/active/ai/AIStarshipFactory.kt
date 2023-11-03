package net.horizonsend.ion.server.features.starship.active.ai

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipComputers
import net.horizonsend.ion.server.features.starship.StarshipDealers
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.util.NPCFakePilot
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World

object AIStarshipFactory : IonServerComponent() {
	fun createAIShipFromTemplate(
		template: AIShipConfiguration.AIStarshipTemplate,
		location: Location,
		createController: (ActiveControlledStarship) -> Controller,
		pilotName: Component?,
		callback: (ActiveControlledStarship) -> Unit = {}
	) {
		val schematic = template.getSchematic()

		if (schematic == null) {
			log.warn("Schematic not found for ${template.identifier} at ${template.schematicFile.toURI()}")
			return
		}

		createFromClipboard(
			location,
			schematic,
			template.type,
			template.miniMessageName,
			pilotName,
			createController,
			callback
		)
	}

	fun createFromClipboard(
		location: Location,
		clipboard: Clipboard,
		type: StarshipType,
		starshipName: String,
		pilotName: Component?,
		createController: (ActiveControlledStarship) -> Controller,
		callback: (ActiveControlledStarship) -> Unit = {}
	) {
		val target = StarshipDealers.resolveTarget(clipboard, location)
		val vec3i = Vec3i(target)

		placeSchematicEfficiently(clipboard, location.world, vec3i, true) {
			tryPilotWithController(location.world, vec3i, type, starshipName, createController) {
				// Set the initial NoOp controller's name to the end name, for access from whatever replaces it in the callback
				pilotName?.let { _ -> (it.controller as NoOpController).pilotName = pilotName }
				callback(it)

				NPCFakePilot.add(it, null, pilotName)
			}
		}
	}

	private fun tryPilotWithController(
		world: World,
		origin: Vec3i,
		type: StarshipType,
		name: String,
		createController: (ActiveControlledStarship) -> Controller,
		callback: (ActiveControlledStarship) -> Unit = {}
	) {
		val block = world.getBlockAtKey(origin.toBlockKey())

		if (block.type != StarshipComputers.COMPUTER_TYPE) {
			warnDetectionFailure("${block.type} at $origin was not a starship computer, failed to pilot", origin)
			return
		}

		DeactivatedPlayerStarships.createAIShipAsync(block.world, block.x, block.y, block.z, type, name) { data ->
			Tasks.async {
				try {
					val state = StarshipDetection.detectNewState(data, detector = debugAudience, loadChunks = true)

					DeactivatedPlayerStarships.updateState(data, state)

					Tasks.sync { PilotedStarships.activateWithoutPilot(debugAudience, data, createController, callback) }
				} catch (e: StarshipDetection.DetectionFailedException) {
					warnDetectionFailure("Detection failed: ${e.message}", origin)
				}
			}
		}
	}

	fun warnDetectionFailure(reason: String, computerLoc: Long) = warnDetectionFailure(reason, Vec3i(computerLoc))

	fun warnDetectionFailure(reason: String, computerLoc: Vec3i) =
		log.warn("Could not activate AI ship at ${computerLoc}! " + reason)
}
