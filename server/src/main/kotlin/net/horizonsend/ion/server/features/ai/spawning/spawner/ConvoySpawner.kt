package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.convoys.AIConvoyTemplate
import net.horizonsend.ion.server.features.ai.spawning.applyPostSpawnBehavior
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
