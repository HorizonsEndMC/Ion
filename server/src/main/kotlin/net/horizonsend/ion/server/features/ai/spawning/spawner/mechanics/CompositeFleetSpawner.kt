package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.util.WeightedIntegerAmount
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.spawning.ships.spawn
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.fleet.Fleet
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.Location
import org.bukkit.World
import org.slf4j.Logger
import java.util.function.Supplier

/**
 * A spawner that triggers multiple [SpawnerMechanic]s and merges their ships
 * into a single fleet. Ships are spawned in one batch with shared difficulty,
 * coordinated fleet behavior, and grouped messaging.
 *
 * @param individualSpawnMessage 0: ship name 1: x 2: y 3: z: 4: World name
 * @param groupMessage 0: ship name 1: x 2: y 3: z: 4: World name
 */
class CompositeFleetSpawner(
	private val mechanics: List<SpawnerMechanic>,
	private val locationProvider: Supplier<Location?>,
	private val groupMessage: SpawnMessage?,
	private val individualSpawnMessage: SpawnMessage?,
	private val difficultySupplier: (World) -> Supplier<Int>,
	private val targetModeSupplier: Supplier<AITarget.TargetMode>,
	private val fleetSupplier: Supplier<Fleet?> = Supplier { null },
	private val controllerModifier: (AIController) -> Unit
) : SpawnerMechanic() {

	override fun trigger(logger: Logger) {
		if (mechanics.isEmpty()) {
			logger.warn("CompositeSpawner triggered with no components.")
			return
		}

		val spawnOrigin = locationProvider.get()
		if (spawnOrigin == null) {
			debugAudience.debug("CompositeSpawner could not get a location to spawn")
			return
		}

		val aiFleet = if (fleetSupplier.get() == null) Fleets.createAIFleet() else fleetSupplier.get()!!
		val fleetDifficulty = difficultySupplier(spawnOrigin.world).get()
		val shipDifficultySupplier = WeightedIntegerAmount(
			setOf(
				fleetDifficulty - 1 to 0.05,
				fleetDifficulty to 0.9,
				fleetDifficulty + 1 to 0.05
			)
		)

		val allShips = mechanics.flatMap { it.getAvailableShips(draw = true) }
		if (allShips.isEmpty()) {
			logger.info("CompositeSpawner found no ships to spawn.")
			return
		}

		for (ship in allShips) {
			val spawnPoint = spawnOrigin.clone()
			for (offset in ship.offsets) spawnPoint.add(offset.get())
			ship.absoluteHeight?.let { spawnPoint.y = it }

			val minDifficulty = if (fleetDifficulty >= 2) 2 else DifficultyModule.minDifficulty
			val difficulty = shipDifficultySupplier.get().coerceIn(minDifficulty, DifficultyModule.maxDifficulty)
			logger.info("difficulty: $difficulty")

			logger.info("Spawning ${ship.template.identifier} at $spawnPoint")

			ship.spawn(logger, spawnPoint, difficulty, targetModeSupplier.get()) {
				addUtilModule(AIFleetManageModule(this, aiFleet))
				controllerModifier(this@spawn)
				aiFleet.initalized = true
			}

			individualSpawnMessage?.broadcast(spawnPoint, ship.template)
		}

		groupMessage?.broadcast(spawnOrigin, null)
	}

	private fun getShips(): List<SpawnedShip> = mechanics.flatMap { it.getAvailableShips() }

	override fun getAvailableShips(draw: Boolean): Collection<SpawnedShip> = getShips()
}

