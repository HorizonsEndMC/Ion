package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.faction.AIConvoyTemplate
import net.horizonsend.ion.server.features.ai.spawning.applyPostSpawnBehavior
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.SpawnerScheduler

class ConvoySpawner(
	identifier: String,
	override val scheduler: SpawnerScheduler,
	template: AIConvoyTemplate
) : AISpawner(
	identifier,
	applyPostSpawnBehavior(template.spawnMechanicBuilder(), template.postSpawnBehavior)
) {
	init {
		scheduler.setSpawner(this)
	}
}
