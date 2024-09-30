package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

abstract class MultiSpawner(private val locationProvider: Supplier<Location?>) : SpawnerMechanic() {
	abstract fun getShips(): List<SpawnedShip>

	override suspend fun trigger(logger: Logger) {
		val ships = getShips()
		if (ships.isEmpty()) {
			debugAudience.debug("Multi spawner didn't get any ships to spawn!")
			return
		}

		val spawnOrigin = locationProvider.get()

		if (spawnOrigin == null) {
			debugAudience.debug("Location provider could not find one")
			return
		}

		val aiFleet = AIFleetManageModule.AIFleet()

		for (ship in ships) {
			val offsets = ship.offsets

			val spawnPoint = spawnOrigin.clone()

			val absoluteHeight = ship.absoluteHeight

			for (offset in offsets) {
				spawnPoint.add(offset.get())
			}

			if (absoluteHeight != null) {
				spawnPoint.y = absoluteHeight
			}

			debugAudience.debug("Spawning ${ship.template.identifier} at $spawnPoint")

			ship.spawn(logger, spawnPoint) {
				modules["fleet"] = AIFleetManageModule(this, aiFleet)
			}
		}
	}
}
