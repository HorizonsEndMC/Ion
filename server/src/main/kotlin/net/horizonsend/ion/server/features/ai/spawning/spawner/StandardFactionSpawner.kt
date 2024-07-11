package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.configuration.WorldSettings
import net.horizonsend.ion.server.features.ai.faction.AIFaction
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.isSystemOccupied
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.WeightedShipSupplier
import net.horizonsend.ion.server.features.misc.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandomOrNull
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

/**
 * A standard AI spawner, spawns ships one at a time
 **/
class StandardFactionSpawner(
	identifier: String,
	logger: Logger,
	val faction: AIFaction,

	/** 0: x, 1: y, 2: z, 3: world name, */
	private val spawnMessage: Component,

	override val pointChance: Double,
	override val pointThreshold: Int,

	val worlds: List<WorldSettings>,
) : AISpawner(
	identifier,
	logger,
	SingleSpawn(
		logger,
		WeightedShipSupplier(*worlds.flatMap { it.templates }.toTypedArray()),
		Supplier {
			val occupiedWorlds = worlds.filter { isSystemOccupied(it.getWorld()) }
			val worldConfig = occupiedWorlds.weightedRandomOrNull { it.probability } ?: return@Supplier null
			val bukkitWorld = worldConfig.getWorld()

			return@Supplier formatLocationSupplier(bukkitWorld, worldConfig.minDistanceFromPlayer, worldConfig.maxDistanceFromPlayer).get()
		},
		faction.controllerModifier,
		faction::getAvailableName
	)
) {

//	override suspend fun triggerSpawn() {
//		val (worldSettings, loc) = findSpawnLocationNearPlayer() ?: return logger.warn("Could not find spawn location!")
//		val (x, y, z) = Vec3i(loc)
//
////		spawnMessage?.let {
////			Notify.chatAndGlobal(template(
////				message = it,
////				paramColor = HEColorScheme.HE_LIGHT_GRAY,
////				useQuotesAroundObjects = false,
////				x,
////				y,
////				z,
////				loc.world.name
////			))
////		}
//
//		val (template, pilotName) = getStarshipTemplateForWorld(worldSettings)
//		spawnAIStarship(logger, template, loc, createController(template, pilotName)).await()
//
//		IonServer.server.sendMessage(template(
//			message = spawnMessage,
//			paramColor = HEColorScheme.HE_LIGHT_GRAY,
//			useQuotesAroundObjects = false,
//			template.starshipInfo.componentName(),
//			x,
//			y,
//			z,
//			loc.world.name
//		))
//	}

	/** Selects a starship template off of the configuration, picks, and serializes a name */
	fun getStarshipTemplateForWorld(worldConfig: WorldSettings): Pair<AITemplate, Component> {
		// If the value is null, it is trying to spawn a ship in a world that it is not configured for.
		val template = worldConfig.templates.weightedRandom { it.probability }.template
		val name = faction.getAvailableName()

		return template to name
	}

	fun findSpawnLocationNearPlayer(): Pair<WorldSettings, Location>?  {
		// Get a random world based on the weight in the config
		val occupiedWorlds = worlds.filter { isSystemOccupied(it.getWorld()) }
		val worldConfig = occupiedWorlds.weightedRandomOrNull { it.probability } ?: return null
		val bukkitWorld = worldConfig.getWorld()

		val player = bukkitWorld.players
			.filter { player -> PilotedStarships.isPiloting(player) }
			.filter { !it.hasProtection() && SpaceWorlds.contains(it.world) }
			.randomOrNull() ?: return null

		var iterations = 0

		val border = bukkitWorld.worldBorder

		val planets = Space.getPlanets().filter { it.spaceWorld == bukkitWorld }.map { it.location.toVector() }

		// max 10 iterations
		while (iterations <= 15) {
			iterations++

			val loc = player.location.getLocationNear(worldConfig.minDistanceFromPlayer, worldConfig.maxDistanceFromPlayer)

			if (!border.isInside(loc)) continue
			if (planets.any { it.distanceSquared(loc.toVector()) <= 250000 }) continue

			loc.y = 192.0

			return worldConfig to loc
		}

		return null
	}
}
