package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.util.WeightedIntegerAmount
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.spawning.ships.spawn
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.starship.fleet.Fleet
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.slf4j.Logger
import java.util.function.Supplier

abstract class MultiSpawner(
	private val locationProvider: Supplier<Location?>,
	private val groupMessage: Component?,
	private val individualSpawnMessage: SpawnMessage?,
	private val difficultySupplier: (World) -> Supplier<Int>,
	private val targetModeSupplier: Supplier<AITarget.TargetMode>,
	private val fleetSupplier: Supplier<Fleet?>
) : SpawnerMechanic() {
	abstract fun getShips(): List<SpawnedShip>

	override fun trigger(logger: Logger) {
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

		val aiFleet = if (fleetSupplier.get() == null) Fleets.createAIFleet() else fleetSupplier.get()!!
		val fleetDifficulty = difficultySupplier(spawnOrigin.world).get()
		val shipDifficultySupplier = WeightedIntegerAmount(
			setOf(
				Pair(fleetDifficulty - 1, 0.05),
				Pair(fleetDifficulty, 0.9),
				Pair(fleetDifficulty + 1, 0.05)
			)
		)

		for (spawnedShip in ships) {
			val offsets = spawnedShip.offsets

			val spawnPoint = spawnOrigin.clone()

			val minDifficulty = if (fleetDifficulty >= 2) 2 else DifficultyModule.minDifficulty
			val difficulty = shipDifficultySupplier.get().coerceIn(minDifficulty, DifficultyModule.maxDifficulty)

			val absoluteHeight = spawnedShip.absoluteHeight

			for (offset in offsets) {
				spawnPoint.add(offset.get())
			}

			if (absoluteHeight != null) {
				spawnPoint.y = absoluteHeight
			}

			debugAudience.debug("Spawning ${spawnedShip.template.identifier} at $spawnPoint")

			spawnedShip.spawn(logger, spawnPoint, difficulty, targetModeSupplier.get()) {
				addUtilModule(AIFleetManageModule(this, aiFleet))
				aiFleet.initalized = true
			}

			individualSpawnMessage?.broadcast(spawnPoint, spawnedShip.template)
		}

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
}
