package net.horizonsend.ion.server.features.ai.spawning.spawner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.ai.spawning.SpawningException
import net.horizonsend.ion.server.features.ai.spawning.handleException
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.AISpawnerTicker
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.SpawnerScheduler
import org.bukkit.World
import org.slf4j.Logger

class LazyWorldSpawner(
	id: String,
	private val worldFilter: (World) -> Boolean,
	private val mechanicSupplier: () -> SpawnerMechanic,
	scheduler: SpawnerScheduler = AISpawnerTicker(pointChance = 0.0, pointThreshold = 100)
) : GlobalWorldSpawner(
	id,
	scheduler,
	/* temporary dummy – we replace it the moment trigger() runs */
	object : SpawnerMechanic() {
		override fun trigger(logger: Logger) {}
		override fun getAvailableShips(draw: Boolean): Collection<SpawnedShip> = listOf()
	}
) {

	/** Build (or reuse) the mechanic and delegate. */
	override fun trigger(logger: Logger, scope: CoroutineScope) = scope.launch {
		val mech = mechanicSupplier()
		try {
			mech.trigger(logger)
		} catch (e: SpawningException) {
			handleException(logger, e)
		} catch (e: Throwable) {
			logger.error("An error occurred when attempting to execute spawner: ${e.message}")
			e.printStackTrace()
		}
	}
}
