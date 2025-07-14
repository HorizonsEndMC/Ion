package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.util.WeightedIntegerAmount
import net.horizonsend.ion.server.features.ai.module.misc.AIDifficulty
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.spawning.ships.spawn
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.Location
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import java.util.function.Supplier

/**
 * A spawner that triggers multiple [SpawnerMechanic]s and merges their ships
 * into a single fleet. Ships are spawned in one batch with shared difficulty,
 * coordinated fleet behavior, and grouped messaging.
 */
class CompositeSpawner(
	private val components: List<SpawnerMechanic>,
	private val locationProvider: Supplier<Location?>,
	private val groupMessage: Component?,
	private val individualSpawnMessage: SpawnMessage?,
	private val difficultySupplier: (String) -> Supplier<Int>,
	private val targetModeSupplier: Supplier<AITarget.TargetMode>,
	private val onPostSpawn: (AIController) -> Unit
) : SpawnerMechanic() {

	override suspend fun trigger(logger: Logger) {
		if (components.isEmpty()) {
			logger.warn("CompositeSpawner triggered with no components.")
			return
		}

		val spawnOrigin = locationProvider.get()
		if (spawnOrigin == null) {
			debugAudience.debug("CompositeSpawner could not get a location to spawn")
			return
		}

		val aiFleet = Fleets.createAIFleet()
		val fleetDifficulty = difficultySupplier(spawnOrigin.world.name).get()
		val shipDifficultySupplier = WeightedIntegerAmount(
			setOf(
				fleetDifficulty - 1 to 0.05,
				fleetDifficulty to 0.9,
				fleetDifficulty + 1 to 0.05
			)
		)

		val allShips = components.flatMap { it.getAvailableShips() }
		if (allShips.isEmpty()) {
			logger.info("CompositeSpawner found no ships to spawn.")
			return
		}

		for (ship in allShips) {
			val spawnPoint = spawnOrigin.clone()
			for (offset in ship.offsets) spawnPoint.add(offset.get())
			ship.absoluteHeight?.let { spawnPoint.y = it }

			val difficulty = shipDifficultySupplier.get()
				.coerceIn(DifficultyModule.minDifficulty.ordinal, DifficultyModule.maxDifficulty.ordinal)
			println("difficulty: $difficulty")

			debugAudience.debug("Spawning ${ship.template.identifier} at $spawnPoint")

			ship.spawn(logger, spawnPoint, AIDifficulty.fromInt(difficulty) ?: AIDifficulty.EASY,targetModeSupplier.get()) {
				addUtilModule(AIFleetManageModule(this, aiFleet))
				onPostSpawn(this)
			}

			individualSpawnMessage?.broadcast(spawnPoint, ship.template)

		}
		aiFleet.initalized = true

		if (aiFleet.members.isNotEmpty() && groupMessage != null) {
			IonServer.server.sendMessage(
				template(
					groupMessage,
					paramColor = HEColorScheme.HE_LIGHT_GRAY,
					useQuotesAroundObjects = false,
					spawnOrigin.blockX,
					spawnOrigin.blockY,
					spawnOrigin.blockZ,
					spawnOrigin.world.name
				)
			)
		}
	}

	private fun getShips(): List<SpawnedShip> = components.flatMap { it.getAvailableShips() }

	override fun getAvailableShips(): Collection<SpawnedShip> = getShips()
}

