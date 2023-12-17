package net.horizonsend.ion.server.features.starship.ai.spawning

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.misc.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipComputers
import net.horizonsend.ion.server.features.starship.StarshipDealers
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.modules.AIRewardsProvider
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getRadialRandomPoint
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player


fun createAIShipFromTemplate(
	template: AIShipConfiguration.AIStarshipTemplate,
	location: Location,
	createController: (ActiveControlledStarship) -> Controller,
	callback: (ActiveControlledStarship) -> Unit = {}
) {
	val schematic = template.getSchematic() ?: throw
	AISpawner.SpawningException(
		"Schematic not found for ${template.identifier} at ${template.schematicFile.toURI()}",
		location.world,
		Vec3i(location)
	)

	createFromClipboard(
		location,
		schematic,
		template.type,
		template.miniMessageName,
		createController
	) {
		it.rewardsProvider = AIRewardsProvider(it, template)
		callback(it)
	}
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
		} catch (e: AISpawner.SpawningException) {
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
		throw AISpawner.SpawningException("${block.type} at $origin was not a starship computer, failed to pilot", world, origin)
	}

	DeactivatedPlayerStarships.createAIShipAsync(block.world, block.x, block.y, block.z, type, name) { data ->
		Tasks.async {
			try {
				val state = StarshipDetection.detectNewState(data, detector = debugAudience, loadChunks = true)

				DeactivatedPlayerStarships.updateState(data, state)

				Tasks.sync { PilotedStarships.activateWithoutPilot(debugAudience, data, createController, callback) }
			} catch (e: StarshipDetection.DetectionFailedException) {
				throw AISpawner.SpawningException("Detection failed: ${e.message}", world, origin)
			}
		}
	}
}

fun getNonProtectedPlayer(world: World): Player? = world.players.filter { !it.hasProtection() }.randomOrNull()

fun isSystemOccupied(world: World): Boolean {
	// Ensure that it is a system with a star and planets
	val planets = Space.getPlanets().filter { it.spaceWorld == world }

	val players = world.players

	planets.flatMapTo(players) { it.planetWorld?.players ?: listOf() }

	return players.isNotEmpty()
}

fun Player.getLocationNear(minDistance: Double, maxDistance: Double): Location {
	val (x, z) = getRadialRandomPoint(minDistance, maxDistance)

	val loc = location.clone()

	return loc.add(x, 0.0, z)
}
