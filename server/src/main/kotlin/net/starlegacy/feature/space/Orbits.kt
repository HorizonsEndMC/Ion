package net.starlegacy.feature.space

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.common.database.schema.space.Planet
import net.starlegacy.util.Tasks
import net.starlegacy.util.blockplacement.BlockPlacement
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

object Orbits : IonServerComponent() {
	// schedule orbiting all the planets every midnight
	override fun onEnable() {
// 		Tasks.sync {
// 			orbitPlanets()
// 		}

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

	fun orbitPlanets(urgent: Boolean = false) {
		// Orbit all the planets
		log.info("Calculating planet orbits...")

		val elapsedNanos = measureNanoTime {
			Space.getPlanets().parallelStream()
				.filter { it.spaceWorld != null }
				.filter { !it.rogue }
				.forEach { it.orbit(urgent = urgent, updateDb = false) }

			Space.getPlanets().parallelStream()
				.filter { it.spaceWorld != null }
				.filter { it.rogue }
				.forEach { it.setLocation(urgent = urgent, updateDb = false) }

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
