package net.horizonsend.ion.server.features.ai.spawning

import com.sk89q.worldedit.extent.clipboard.Clipboard
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.misc.GlowModule
import net.horizonsend.ion.server.features.ai.starship.StarshipTemplate
import net.horizonsend.ion.server.features.npcs.StarshipDealers
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipComputers
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.modules.AISinkMessageFactory
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.blockplacement.BlockPlacement
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.slf4j.Logger
import java.util.function.Supplier

/** Handle any exceptions with spawning */
fun handleException(logger: Logger, exception: SpawningException) {
	logger.warn("AI spawning encountered an issue: ${exception.message}, attempting to spawn a ship at ${exception.spawningLocation}")

	val blockKeys = exception.blockLocations

	// Delete a ship that did not detect properly
	if (blockKeys.isNotEmpty()) {
		val airQueue = Long2ObjectOpenHashMap<BlockState>(blockKeys.size)
		val air = Blocks.AIR.defaultBlockState()

		blockKeys.associateWithTo(airQueue) { air }

		BlockPlacement.placeImmediate(exception.world, airQueue)
	}
}

fun createAIShipFromTemplate(
	logger: Logger,
	template: AITemplate,
	location: Location,
	createController: (ActiveControlledStarship) -> Controller,
	callback: (ActiveControlledStarship) -> Unit = {}
) = createShipFromTemplate(logger, template.starshipInfo, location, createController) { starship ->
	starship.rewardsProviders.addAll(template.rewardProviders.map { it.createRewardsProvider(starship, template) })
	starship.sinkMessageFactory = AISinkMessageFactory(starship)
	(starship.controller as AIController).modules["Glow"] = GlowModule(starship.controller as AIController)

	callback(starship)
}

fun createShipFromTemplate(
    logger: Logger,
    template: StarshipTemplate,
    location: Location,
    createController: (ActiveControlledStarship) -> Controller,
    callback: (ActiveControlledStarship) -> Unit = {}
) {
	val schematic = template.getSchematic() ?: throw SpawningException(
		"Schematic not found for ${template.schematicName} at ${template.schematicFile.toURI()}",
		location.world,
		Vec3i(location)
	)

	createFromClipboard(
		logger,
		location,
		schematic,
		template.type,
		template.miniMessageName,
		createController
	) { starship ->
		callback(starship)
	}
}

fun createFromClipboard(
	logger: Logger,
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
				logger,
				location.world,
				vec3i,
				type,
				starshipName,
				createController,
				callback
			)
		} catch (e: SpawningException) {
			e.blockLocations = it
			throw e
		}
	}
}

private fun tryPilotWithController(
	logger: Logger,
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
		throw SpawningException("${block.type} at $origin was not a starship computer, failed to pilot", world, origin)
	}

	DeactivatedPlayerStarships.createAIShipAsync(block.world, block.x, block.y, block.z, type, name) { data ->
		Tasks.async {
			try {
				val state = StarshipDetection.detectNewState(data, detector = debugAudience, loadChunks = true)

				DeactivatedPlayerStarships.updateState(data, state)

				Tasks.sync { PilotedStarships.activateWithoutPilot(debugAudience, data, createController, callback) }
			}
			catch (e: SpawningException) { handleException(logger, e) }
			catch (e: Throwable) {
				logger.error("An error occurred when attempting to pilot starship ${e.message}")
				e.printStackTrace()
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

fun formatLocationSupplier(world: World, minDistance: Double, maxDistance: Double, playerFilter: (Player) -> Boolean = { true }): Supplier<Location?> = Supplier {
	val player = world.players
		.filter { player -> PilotedStarships.isPiloting(player) }
		.filter(playerFilter)
		.randomOrNull() ?: return@Supplier null

	var iterations = 0

	val border = world.worldBorder

	val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

	// max 10 iterations
	while (iterations <= 15) {
		iterations++

		val loc = player.location.getLocationNear(minDistance, maxDistance)

		if (!border.isInside(loc)) continue
		if (planets.any { it.distanceSquared(loc.toVector()) <= 250000 }) continue

		loc.y = 192.0

		return@Supplier loc
	}

	return@Supplier null
}
