package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.configuration.util.WeightedIntegerAmount
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.util.function.Supplier

class DifficultyModule(
	controller: AIController,
	var internalDifficulty: AIDifficulty = AIDifficulty.HARD
) : AIModule(controller){
	val isShieldAware get() = internalDifficulty >= AIDifficulty.HARD

	val doBackOff get() = internalDifficulty >= AIDifficulty.HARD

	val speedDebuff get () = internalDifficulty <= AIDifficulty.HARD

	val outOfRangeAggro : Double get() {
		return when (internalDifficulty) {
			AIDifficulty.EASY -> 0.0
			AIDifficulty.NORMAL -> 0.0
			AIDifficulty.HARD -> 0.5
			AIDifficulty.HARDER -> 1.0
			AIDifficulty.INSANE -> 2.0
		}
	}

	val decayEmityThreshold : Double get() {
		return when (internalDifficulty) {
			AIDifficulty.EASY -> 0.0
			AIDifficulty.NORMAL -> 0.0
			AIDifficulty.HARD -> 0.0
			AIDifficulty.HARDER -> 0.5
			AIDifficulty.INSANE -> 1.5
		}
	}

	val doNavigation : Boolean get() = internalDifficulty >= AIDifficulty.HARD

	val shotVariation : Double get() {
		return when (internalDifficulty) {
			AIDifficulty.EASY -> 0.3
			AIDifficulty.NORMAL -> 0.15
			else -> 0.0
		}
	}

	val doubleEstimateAim get() = internalDifficulty >= AIDifficulty.INSANE

	val aimEverything get() = internalDifficulty >= AIDifficulty.INSANE

	val faceModifier get() = if (internalDifficulty <= AIDifficulty.EASY) 0.5 else 1.0

	val aimAdjust : Double get() {
		return when (internalDifficulty) {
			AIDifficulty.EASY -> 0.0
			AIDifficulty.NORMAL -> 0.0
			AIDifficulty.HARD -> 0.5
			AIDifficulty.HARDER -> 1.0
			AIDifficulty.INSANE -> 1.0
		}
	}

	val powerModeSwitch get() = internalDifficulty >= AIDifficulty.HARD
	val useSpecialPowerModes  get() = internalDifficulty >= AIDifficulty.INSANE

	val rewardMultiplier : Double get() {
		return when (internalDifficulty) {
			AIDifficulty.EASY -> 0.7
			AIDifficulty.NORMAL -> 0.9
			AIDifficulty.HARD -> 1.0
			AIDifficulty.HARDER -> 1.15
			AIDifficulty.INSANE -> 1.3
		}
	}


	companion object {

		val maxDifficulty = AIDifficulty.entries.max()
		val minDifficulty = AIDifficulty.entries.min()

		fun regularSpawnDifficultySupplier(world: String) : Supplier<Int> {
			//println(world)
			return when (world) {
				"Trench" -> WeightedIntegerAmount(setOf(
					Pair(0,0.15),
					Pair(1,0.35),
					Pair(2,0.35),
					Pair(3,0.10)
				))
				"AU-0821" -> WeightedIntegerAmount(setOf(
					Pair(0,0.0),
					Pair(1,0.1),
					Pair(2,0.55),
					Pair(3,0.25),
					Pair(4,0.1),
				))
				"Horizon" -> WeightedIntegerAmount(setOf(
					Pair(0,0.2),
					Pair(1,0.35),
					Pair(2,0.4),
					Pair(3,0.05),
					))
				"Asteri" -> WeightedIntegerAmount(setOf(
					Pair(0,0.5),
					Pair(1,0.35),
					Pair(2,0.15)))
				"Ilios" -> WeightedIntegerAmount(setOf(
					Pair(0,0.5),
					Pair(1,0.35),
					Pair(2,0.15)))
				"Sirius" -> WeightedIntegerAmount(setOf(
					Pair(0,0.5),
					Pair(1,0.35),
					Pair(2,0.15)))
				"Regulus" -> WeightedIntegerAmount(setOf(
					Pair(0,0.5),
					Pair(1,0.35),
					Pair(2,0.15)))

				else -> WeightedIntegerAmount(setOf(
					Pair(0,0.3),
					Pair(1,0.4),
					Pair(2,0.4)))
			}
		}
	}
}

enum class AIDifficulty {
	EASY,
	NORMAL,
	HARD,
	HARDER,
	INSANE;

	companion object {
		fun fromInt(value: Int) = AIDifficulty.entries.firstOrNull { it.ordinal == value }
	}
}
