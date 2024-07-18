package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import org.bukkit.World
import org.slf4j.Logger

class SingleWorldSpawner(
	identifier: String,
	val world: World,
	override val pointChance: Double,
	override val pointThreshold: Int,
	mechanic: SpawnerMechanic
) : AISpawner(identifier, mechanic) {
	override fun tickPoints(logger: Logger) {
		if (world.players.isEmpty()) return

		super.tickPoints(logger)
	}
}

