package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner

interface SpawnerScheduler {
	fun getSpawner(): AISpawner

	fun setSpawner(spawner: AISpawner): SpawnerScheduler

	fun getTickInfo(): String

	class DummyScheduler(private val spawner: AISpawner) : SpawnerScheduler {
		override fun getSpawner(): AISpawner = spawner
		override fun setSpawner(spawner: AISpawner): SpawnerScheduler { return this }

		override fun getTickInfo(): String = "Dummy scheduler"
	}
}
