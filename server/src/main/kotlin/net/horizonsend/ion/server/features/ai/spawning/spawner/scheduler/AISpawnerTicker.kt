package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import org.slf4j.Logger
import kotlin.random.Random

class AISpawnerTicker(
	private val pointChance: Double = 1.0,
	private val pointThreshold: Int
) : SpawnerScheduler, TickedScheduler {
	private lateinit var spawner: AISpawner

	override fun getSpawner(): AISpawner {
		return spawner
	}

	override fun setSpawner(spawner: AISpawner): SpawnerScheduler {
		this.spawner = spawner
		return this
	}

	var points: Int = 0
	private var lastTriggered: Long = 0

	/** Tick points, possibly trigger a spawn */
	override fun tick(logger: Logger) {
		handleSuccess(logger)

		if (Random.nextDouble() >= pointChance) return

		points++
	}

	private fun handleSuccess(logger: Logger) {
		if (points < pointThreshold) return

		points = 0

		lastTriggered = System.currentTimeMillis()
		spawner.trigger(logger, AISpawningManager.context)
	}

	override fun getTickInfo(): String {
		return "$points points, $pointThreshold required"
	}
}
