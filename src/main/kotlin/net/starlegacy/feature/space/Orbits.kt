package net.starlegacy.feature.space

import net.starlegacy.SLComponent
import net.starlegacy.database.schema.space.Planet
import net.starlegacy.feature.misc.AutoRestart
import net.starlegacy.util.Tasks
import net.starlegacy.util.blockplacement.BlockPlacement
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

object Orbits : SLComponent() {
    // schedule orbiting all the planets every midnight
    override fun onEnable() {
        if (AutoRestart.isRestart) {
            log.info("Queued orbit")
            Tasks.sync {
                orbitPlanets()
            }
        } else {
            log.warn("Not orbiting because not an automatic restart")
        }

//        schedule()
    }

    private fun schedule() {
        Tasks.asyncAtHour(4) {
            schedule()
            Tasks.sync {
                orbitPlanets()
            }
        }
    }

    fun orbitPlanets() {
        // Orbit all the planets
        log.info("Calculating planet orbits...")

        val elapsedNanos = measureNanoTime {
            Space.getPlanets().parallelStream()
                .filter { it.spaceWorld != null }
                .forEach { it.orbit(updateDb = false) }

            SpaceMap.refresh()
        }

        var elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)
        log.info("  -> $elapsedMillis milliseconds elapsed")

        log.info("Flushing queue...")

        BlockPlacement.flush { world ->
            log.info("Finished planets in ${world.name}, saving...")

            for (planet in Space.getPlanets().filter { it.spaceWorld == world }) {
                Planet.setOrbitProgress(planet.databaseId, planet.orbitProgress)
            }

            world.save()

            elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)
            log.info("  -> $elapsedMillis milliseconds elapsed")
        }
    }

    override fun supportsVanilla(): Boolean {
        return true
    }
}
