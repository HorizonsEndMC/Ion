package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.configuration.WorldSettings
import net.horizonsend.ion.server.features.ai.spawning.SpawnerScheduler
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.isSystemOccupied
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.WeightedShipSupplier
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandomOrNull
import net.kyori.adventure.text.Component
import java.util.function.Supplier

/**
 * A standard AI spawner, spawns ships one at a time
 **/
class StandardFactionSpawner(
	identifier: String,
	override val scheduler: SpawnerScheduler,
	spawnMessage: Component,
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
		},
		spawnMessage
	)
) {
	init {
		scheduler.setSpawner(this)
	}
}
