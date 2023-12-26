package net.horizonsend.ion.server.features.starship.ai.spawning.explorer

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.ai.spawning.findSpawnLocationNearPlayer
import net.horizonsend.ion.server.features.starship.ai.spawning.getNonProtectedPlayer
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
import net.kyori.adventure.text.Component
import org.bukkit.Location

class ExplorerSingleSpawner : BasicSpawner(
	"EXPLORER_SINGLE",
	IonServer.aiSpawningConfiguration.spawners::explorerSingle,
) {
	override fun findSpawnLocation(): Location? {
		// Get a random world based on the weight in the config
		val worldConfig = configuration.worldWeightedRandomList.random()
		val world = worldConfig.getWorld()

		val player = getNonProtectedPlayer(world) ?: return findSpawnLocationNearPlayer(configuration) { SpaceWorlds.contains(it.world) }

		var iterations = 0

		val border = world.worldBorder

		val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

		// max 10 iterations
		while (iterations <= 15) {
			iterations++

			val loc = player.location.getLocationNear(configuration.minDistanceFromPlayer, configuration.maxDistanceFromPlayer)

			if (!border.isInside(loc)) continue

			if (planets.any { it.distanceSquared(loc.toVector()) <= 250000 }) continue

			return loc
		}

		return null
	}

	override val spawnMessage: Component = ofChildren(
		Component.text("[", HEColorScheme.HE_LIGHT_GRAY),
		Component.text("{3} System Alert", EXPLORER_LIGHT_CYAN),
		Component.text("]", HEColorScheme.HE_LIGHT_GRAY),
		Component.text(" A Horizon Transit Lines vessel will be passing through the system.", EXPLORER_MEDIUM_CYAN)
	)

	companion object {
		val defaultConfiguration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<$PRIVATEER_LIGHT_TEAL>Horizon Transit Lines<${HEColorScheme.HE_MEDIUM_GRAY.asHexString()}> {0} spawned at {1}, {2}, {3}, in {4}",
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 15,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "SMALL",
					nameList = mapOf(
						"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot" to 5,
//						"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot" to 3,
					),
					ships = mapOf(
						nimble.identifier to 4,
						striker.identifier to 2,
						minhaulCheth.identifier to 7,
						minhaulRedstone.identifier to 3,
						minhaulTitanium.identifier to 4,
					)
				),
				AISpawningConfiguration.AISpawnerTier(
					identifier = "INTERMEDIATE",
					nameList = mapOf(
						"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot" to 5,
					),
					ships = mapOf(
						minhaulCheth.identifier to 21,
						minhaulRedstone.identifier to 9,
						minhaulTitanium.identifier to 12,
						exotranChetherite.identifier to 2,
						exotranRedstone.identifier to 2,
						exotranTitanium.identifier to 2,
						wayfinder.identifier to 2,
						striker.identifier to 4,
						amph.identifier to 2,
					)
				),
				AISpawningConfiguration.AISpawnerTier(
					identifier = "MEDIUM",
					nameList = mapOf(
						"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot" to 5,
					),
					ships = mapOf(
						minhaulCheth.identifier to 2,
						minhaulRedstone.identifier to 2,
						minhaulTitanium.identifier to 2,
						exotranChetherite.identifier to 2,
						exotranRedstone.identifier to 2,
						exotranTitanium.identifier to 2,
						dessle.identifier to 2,
						nimble.identifier to 2,
						wayfinder.identifier to 4,
						striker.identifier to 3,
						amph.identifier to 2,
					)
				),
				AISpawningConfiguration.AISpawnerTier(
					identifier = "LARGE",
					nameList = mapOf(
						"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot" to 5,
					),
					ships = mapOf(
						exotranChetherite.identifier to 6,
						exotranRedstone.identifier to 6,
						exotranTitanium.identifier to 6,
						dessle.identifier to 3,
						nimble.identifier to 2,
						wayfinder.identifier to 5,
						striker.identifier to 2,
						amph.identifier to 2,
					)
				),
			),
			worldSettings = listOf(
				AISpawningConfiguration.AIWorldSettings(world = "Asteri", rolls = 7, tiers = mapOf(
					"SMALL" to 8,
					"INTERMEDIATE" to 2
				)),
				AISpawningConfiguration.AIWorldSettings(world = "Sirius", rolls = 4, tiers = mapOf(
					"SMALL" to 8,
					"INTERMEDIATE" to 4
				)),
				AISpawningConfiguration.AIWorldSettings(world = "Regulus", rolls = 7, tiers = mapOf(
					"SMALL" to 8,
					"INTERMEDIATE" to 2
				)),
				AISpawningConfiguration.AIWorldSettings(world = "Ilios", rolls = 5, tiers = mapOf(
					"SMALL" to 4,
					"INTERMEDIATE" to 2
				)),
				AISpawningConfiguration.AIWorldSettings(world = "Horizon", rolls = 10, tiers = mapOf(
					"INTERMEDIATE" to 7,
					"MEDIUM" to 4,
					"LARGE" to 1,
				)),
				AISpawningConfiguration.AIWorldSettings(world = "Trench", rolls = 2, tiers = mapOf(
					"INTERMEDIATE" to 2,
					"MEDIUM" to 7,
					"LARGE" to 4,
				)),
				AISpawningConfiguration.AIWorldSettings(world = "AU-0821", rolls = 2, tiers = mapOf(
					"INTERMEDIATE" to 2,
					"MEDIUM" to 7,
					"LARGE" to 4,
				))
			)
		)
	}
}
