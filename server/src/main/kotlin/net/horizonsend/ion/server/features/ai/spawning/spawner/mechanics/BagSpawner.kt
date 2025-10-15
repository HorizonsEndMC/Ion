package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.configuration.util.IntegerAmount
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.fleet.Fleet
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import java.util.function.Supplier
import kotlin.math.cbrt

class BagSpawner(
	locationProvider: Supplier<Location?>,
	private val budget: Supplier<Int>,
	groupMessage: Component?,
	individualSpawnMessage: SpawnMessage?,
	difficultySupplier: (World) -> Supplier<Int>,
	targetModeSupplier: Supplier<AITarget.TargetMode>,
	fleetSupplier: Supplier<Fleet?> = Supplier { null },
	vararg bagSpawnedShips: BagSpawnShip
) : MultiSpawner(locationProvider, groupMessage, individualSpawnMessage, difficultySupplier, targetModeSupplier, fleetSupplier) {
	private val bagSpawnedShips: List<BagSpawnShip> = listOf(*bagSpawnedShips)

	override fun getShips(): List<SpawnedShip> {
		var points = budget.get()
		println("Bag Spawn Points: $points")
		val ships = mutableListOf<SpawnedShip>()

		while (points > 0) {
			val remainingAvailable = bagSpawnedShips.filter { it.cost <= points }
			if (remainingAvailable.isEmpty()) break

			val ship = remainingAvailable.random()

			points -= ship.cost
			ships += ship.ship
		}

		return ships
	}

	data class BagSpawnShip(
		val ship: SpawnedShip,
		val cost: Int,
	)

	companion object {
		fun asBagSpawned(ship: SpawnedShip, cost: Int) = BagSpawnShip(ship, cost)

		/** Curried helper function to spawn in a bag spawner as a reinforcement ship*/
		fun asReinforcement(
			locationProvider: Supplier<Location?>,
			budget: Supplier<Int>,
			groupMessage: Component?,
			individualSpawnMessage: SpawnMessage?,
			vararg bagSpawnedShips: BagSpawnShip,
		): (AIController) -> SpawnerMechanic {

			return { controller ->
				val internalDifficulty = controller.getCoreModuleByType<DifficultyModule>()?.internalDifficulty
				val targetMode = controller.getCoreModuleByType<EnmityModule>()?.targetMode

				val difficultySupplier: (World) -> Supplier<Int> =
					if (internalDifficulty != null) { _: World -> Supplier { internalDifficulty } }
					else DifficultyModule.Companion::regularSpawnDifficultySupplier

				val targetSupplier: Supplier<AITarget.TargetMode> =
					if (targetMode != null) Supplier { targetMode }
					else Supplier { AITarget.TargetMode.PLAYER_ONLY }


				//check if reinforced ship is part of a fleet.
				var fleet = controller.getUtilModule(AIFleetManageModule::class.java)?.fleet
				if (fleet == null) {
					fleet = Fleets.createAIFleet()
					controller.addUtilModule(AIFleetManageModule(controller, fleet))
				}
				val fleetSupplier: Supplier<Fleet?> = Supplier { fleet }

				BagSpawner(
					locationProvider = locationProvider,
					budget = budget,
					groupMessage = groupMessage,
					individualSpawnMessage = individualSpawnMessage,
					bagSpawnedShips = bagSpawnedShips,
					difficultySupplier = difficultySupplier,
					targetModeSupplier = targetSupplier,
					fleetSupplier = fleetSupplier
				)
			}
		}

		fun withFleetScaling(
			baseSupplier : Supplier<Int>,
			locationSupplier : Supplier<Location>,
			shipWeight : Double = 1.0,
			superCapitalWeight : Double = 3.0,
			threshold: Int = 19,
		) : Supplier<Int> {
			return Supplier {
				val baseBudget = baseSupplier.get()
				val location = locationSupplier.get()
				//get all nearby starships TODO: make sure to grab also inactive ships
				val ships = ActiveStarships.getInWorld(location.world).filter { it.controller is PlayerController
					&& it.centerOfMass.toVector().distance(location.toVector()) <= 2000.0
				}
				var cumulativeWeight = 0.0
				ships.forEach { ship ->
					val weight = (cbrt(ship.initialBlockCount.toDouble()) * shipWeight)
					cumulativeWeight += if (ship.initialBlockCount > 12000) {
						weight * superCapitalWeight
					} else {
						weight
					}
				}
				cumulativeWeight = (cumulativeWeight - threshold).coerceAtLeast(0.0)
				baseBudget + cumulativeWeight.toInt()
			}
		}
	}

	override fun getAvailableShips(draw: Boolean): Collection<SpawnedShip> {
		if (!draw) return bagSpawnedShips.map { it.ship }
		return getShips()
	}
}
