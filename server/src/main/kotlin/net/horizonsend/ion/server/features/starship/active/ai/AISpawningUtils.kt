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
import net.horizonsend.ion.server.features.starship.active.ai.spawning.Spawner
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import org.bukkit.Location
import org.bukkit.World

object AISpawningUtils : IonServerComponent() {
	fun createAIShipFromTemplate(
		template: AIShipConfiguration.AIStarshipTemplate,
		location: Location,
		createController: (ActiveControlledStarship) -> Controller,
		callback: (ActiveControlledStarship) -> Unit = {}
	) {
		val schematic = template.getSchematic() ?: throw
			Spawner.SpawningException(
				"Schematic not found for ${template.identifier} at ${template.schematicFile.toURI()}",
				location.world,
				Vec3i(location)
			)

		createFromClipboard(
			location,
			schematic,
			template.type,
			template.miniMessageName,
			createController,
			callback
		)
	}

	fun createFromClipboard(
		location: Location,
		clipboard: Clipboard,
		type: StarshipType,
		starshipName: String,
		createController: (ActiveControlledStarship) -> Controller,
		callback: (ActiveControlledStarship) -> Unit = {}
	) {
		val target = StarshipDealers.resolveTarget(clipboard, location)
		val vec3i = Vec3i(target)

		placeSchematicEfficiently(clipboard, location.world, vec3i, true) {
			try {
				tryPilotWithController(
					location.world,
					vec3i,
					type,
					starshipName,
					createController,
					callback
				)
			} catch (e: Spawner.SpawningException) {
				e.blockLocations = it

				throw e
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
		val (x, y, z) = origin
		val block = world.getBlockAt(x, y, z)

		if (block.type != StarshipComputers.COMPUTER_TYPE) {
			throw Spawner.SpawningException("${block.type} at $origin was not a starship computer, failed to pilot", world, origin)
		}

		DeactivatedPlayerStarships.createAIShipAsync(block.world, block.x, block.y, block.z, type, name) { data ->
			Tasks.async {
				try {
					val state = StarshipDetection.detectNewState(data, detector = debugAudience, loadChunks = true)

					DeactivatedPlayerStarships.updateState(data, state)

					Tasks.sync { PilotedStarships.activateWithoutPilot(debugAudience, data, createController, callback) }
				} catch (e: StarshipDetection.DetectionFailedException) {
					throw Spawner.SpawningException("Detection failed: ${e.message}", world, origin)
				}
			}
		}
	}
}
