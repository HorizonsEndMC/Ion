package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * @param check returns whether this should continue
 **/
class SpawnWarmupTask(
	val delay: Duration,
	val check: () -> Boolean,
	val execute: () -> Unit
) : BukkitRunnable(), SpawnerScheduler {
	private lateinit var spawner: AISpawner

	override fun getSpawner(): AISpawner {
		return spawner
	}

	override fun setSpawner(spawner: AISpawner): SpawnerScheduler {
		this.spawner = spawner
		schedule()
		return this
	}

	override fun run() {
		if (!check()) {
			cancel()
			return
		}

		if (Duration.ofMillis(System.currentTimeMillis() - startTime) < delay) {
			return
		}

		Tasks.sync {
			execute.invoke()
		}
	}

	private var startTime = -1L

	private fun schedule() {
		runTaskTimerAsynchronously(IonServer, 20L, 20L)
		startTime = System.currentTimeMillis()
	}

	override fun getTickInfo(): String {
		val elapsed = System.currentTimeMillis() - startTime
		val remaining = delay.toMillis() - elapsed
		return "${TimeUnit.MILLISECONDS.toSeconds(remaining)} seconds remaining"
	}
}
