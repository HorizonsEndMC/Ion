package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.spawning.ships.spawn
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.fleet.Fleet
import org.bukkit.Location
import org.bukkit.World
import org.slf4j.Logger
import java.util.function.Supplier

class SingleSpawn(
	private val shipPool: ShipSupplier,
	private val locationProvider: Supplier<Location?>,
	/** 0: x, 1: y, 2: z, 3: world name, */
	private val spawnMessage: SpawnMessage?,
	private val difficultySupplier: (World) -> Supplier<Int>,
	private val targetModeSupplier: Supplier<AITarget.TargetMode>,
	private val fleetSupplier: Supplier<Fleet?> = Supplier { null },
	private val controllerModifier: AIController.() -> Unit = {}
) : SpawnerMechanic() {
	override fun trigger(logger: Logger) {
		val ship = shipPool.get()
		val spawnPoint = locationProvider.get() ?: return
		val difficulty = difficultySupplier(spawnPoint.world).get()

		ship.spawn(logger, spawnPoint, difficulty, targetModeSupplier.get()) {
			val fleet = fleetSupplier.get()
			if (fleet != null) {
				addUtilModule(AIFleetManageModule(this, fleet))
			}
			controllerModifier
		}

		spawnMessage?.broadcast(spawnPoint, ship.template)
	}

	override fun getAvailableShips(draw: Boolean): Collection<SpawnedShip> {
		if (!draw) return shipPool.getAllAvailable()
		return listOf(shipPool.get())
	}
}
