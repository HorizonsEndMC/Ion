package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.configuration.WorldSettings
import net.horizonsend.ion.server.features.ai.faction.AIFaction
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.isSystemOccupied
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.WeightedShipSupplier
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag.ALLOW_AI_SPAWNS
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandomOrNull
import net.kyori.adventure.text.Component
import org.bukkit.Location
import java.util.function.Supplier

/**
 * A standard AI spawner, spawns ships one at a time
 **/
class StandardFactionSpawner(
	identifier: String,
	val faction: AIFaction,

	/** 0: x, 1: y, 2: z, 3: world name, */
	private val spawnMessage: Component,

	override val pointChance: Double,
	override val pointThreshold: Int,

	val worlds: List<WorldSettings>,
) : AISpawner(
	identifier,
	SingleSpawn(
		WeightedShipSupplier(*worlds.flatMap { it.templates }.toTypedArray()),
		Supplier {
			val occupiedWorlds = worlds.filter { isSystemOccupied(it.getWorld()) }
			val worldConfig = occupiedWorlds.weightedRandomOrNull { it.probability } ?: return@Supplier null
			val bukkitWorld = worldConfig.getWorld()

			return@Supplier formatLocationSupplier(bukkitWorld, worldConfig.minDistanceFromPlayer, worldConfig.maxDistanceFromPlayer).get()
		}
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

	fun findSpawnLocationNearPlayer(): Pair<WorldSettings, Location>?  {
		// Get a random world based on the weight in the config
		val occupiedWorlds = worlds.filter { isSystemOccupied(it.getWorld()) }
		val worldConfig = occupiedWorlds.weightedRandomOrNull { it.probability } ?: return null
		val bukkitWorld = worldConfig.getWorld()

		val player = bukkitWorld.players
			.filter { player -> PilotedStarships.isPiloting(player) }
			.filter { !it.hasProtection() && it.world.ion.hasFlag(ALLOW_AI_SPAWNS) }
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
