package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.SpawnerScheduler

open class GlobalWorldSpawner(
    identifier: String,
    override val scheduler: SpawnerScheduler,
    mechanic: SpawnerMechanic
) : AISpawner(identifier, mechanic) {
	init {
		scheduler.setSpawner(this)
	}
}
