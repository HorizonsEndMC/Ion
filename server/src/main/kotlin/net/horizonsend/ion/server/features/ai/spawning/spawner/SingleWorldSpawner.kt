package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import org.bukkit.World
import org.slf4j.Logger

class SingleWorldSpawner(
	identifier: String,
	val world: World,
	override val pointChance: Double,
	override val pointThreshold: Int,
	logger: Logger,
	mechanic: SpawnerMechanic
) : AISpawner(identifier, logger, mechanic) {
	override fun tickPoints() {
		if (world.players.isEmpty()) return

		super.tickPoints()
	}
}

