package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.configuration.util.IntegerAmount
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.fleet.Fleet
import net.horizonsend.ion.server.features.starship.fleet.Fleets
import net.kyori.adventure.text.Component
import org.bukkit.Location
import java.util.function.Supplier

class BagSpawner(
    locationProvider: Supplier<Location?>,
    private val budget: IntegerAmount,
    groupMessage: Component?,
    individualSpawnMessage: SpawnMessage?,
    vararg bagSpawnedShips: BagSpawnShip,
	difficultySupplier: (String) -> Supplier<Int>,
	targetModeSupplier: Supplier<AITarget.TargetMode>,
	fleetSupplier: Supplier<Fleet?> = Supplier { null }
) : MultiSpawner(locationProvider, groupMessage, individualSpawnMessage, difficultySupplier, targetModeSupplier, fleetSupplier) {
	private val bagSpawnedShips: List<BagSpawnShip> = listOf(*bagSpawnedShips)

	override fun getShips(): List<SpawnedShip> {
		var points = budget.get()
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
			budget: IntegerAmount,
			groupMessage: Component?,
			individualSpawnMessage: SpawnMessage?,
			vararg bagSpawnedShips: BagSpawnShip,
		) : (AIController) -> SpawnerMechanic {

			return {controller ->
				val internalDifficulty = controller.getCoreModuleByType<DifficultyModule>()?.internalDifficulty
				val targetMode = controller.getCoreModuleByType<EnmityModule>()?.targetMode

				val difficultySupplier : (String) -> Supplier<Int> =
					if (internalDifficulty != null)	{_ : String -> Supplier{internalDifficulty}}
					else DifficultyModule.Companion::regularSpawnDifficultySupplier

				val targetSupplier : Supplier<AITarget.TargetMode> =
					if (targetMode != null)	Supplier { targetMode }
					else Supplier { AITarget.TargetMode.PLAYER_ONLY }


				//check if reinforced ship is part of a fleet.
				var fleet = controller.getUtilModule(AIFleetManageModule::class.java)?.fleet
				if (fleet == null) {
					fleet = Fleets.createAIFleet()
					controller.addUtilModule(AIFleetManageModule(controller,fleet))
				}
				val fleetSupplier : Supplier<Fleet?> = Supplier{fleet}

				BagSpawner(
				locationProvider = locationProvider,
				budget = budget,
				groupMessage = groupMessage,
				individualSpawnMessage = individualSpawnMessage,
				bagSpawnedShips = bagSpawnedShips,
				difficultySupplier = difficultySupplier ,
				targetModeSupplier = targetSupplier,
				fleetSupplier = fleetSupplier
			)}
		}
	}

	override fun getAvailableShips(draw: Boolean): Collection<SpawnedShip> {
		if (!draw) return bagSpawnedShips.map { it.ship }
		return getShips()
	}
}
