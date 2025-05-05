
package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.ai.convoys.toContext
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicBoolean
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import net.horizonsend.ion.server.features.economy.city.TradeCities
import org.slf4j.Logger

object CaravanScheduler : SpawnerScheduler,TickedScheduler {

    override fun tick(logger: Logger) {
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        val minute = now.minute
        val hour = now.hour

        if (minute == 0 ) {
            TradeCities.getAll().forEach { city ->
                val scheduled = city.scheduledHour ?: return@forEach
                val convoyTemplate = city.convoyTemplate ?: return@forEach
                val effectiveAfter = city.configEffectiveAfter ?: return@forEach
                if (scheduled != hour || System.currentTimeMillis() < effectiveAfter) return@forEach

				val mechanic = convoyTemplate.spawnMechanicBuilder(city.toContext())

				AISpawningManager.context.launch {
					mechanic.trigger(logger)
				}
            }
        }
    }

	override fun getSpawner(): AISpawner {
		TODO("Not yet implemented")
	}

	override fun setSpawner(spawner: AISpawner): SpawnerScheduler {
		TODO("Not yet implemented")
	}

	override fun getTickInfo(): String = "Spawns daily at city-configured UTC hour (if delay passed)"
}
