package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import java.time.DayOfWeek
import java.time.LocalDate

interface SetTimeScheduler : SpawnerScheduler {
	val hours: List<Int>
	val days: List<DayOfWeek>

	fun schedule() {
		for (hour in hours) {
			Tasks.asyncAtHour(hour) {
				if (!days.contains(LocalDate.now().dayOfWeek)) return@asyncAtHour

				getSpawner().trigger(IonServer.slF4JLogger, AISpawningManager.context)
			}
		}
	}
}
