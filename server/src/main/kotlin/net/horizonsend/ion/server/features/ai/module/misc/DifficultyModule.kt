package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.configuration.util.WeightedIntegerAmount
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.minecraft.util.valueproviders.IntProvider
import java.util.function.Supplier

class DifficultyModule(
	controller: AIController,
	var internalDifficulty: Int = 2
) : AIModule(controller){
	val isShieldAware get() = internalDifficulty >= 2

	val doBackOff get() = internalDifficulty >= 2

	val speedDebuff get () = internalDifficulty <= 0

	val shotVariation : Double get() {
		return when (internalDifficulty) {
			0 -> 0.3
			1 -> 0.15
			else -> 0.0
		}
	}

	val doubleEstimateAim get() = internalDifficulty >= 4

	val aimAdjust : Double get() {
		return when (internalDifficulty) {
			0 -> 0.0
			1 -> 0.0
			2 -> 0.5
			3 -> 1.0
			4 -> 1.0
			else -> {0.0}
		}
	}

	val aimEverything get() = internalDifficulty >= 4

	val faceModifier get() = if (internalDifficulty <= 0) 0.5 else 1.0

	companion object {

		val maxDifficulty = 2
		val minDifficulty = 0

		fun regularSpawnDifficultySupplier(world: String) : Supplier<Int> {
			return when (world) {
				"Trench" -> WeightedIntegerAmount(setOf(
					Pair(0,0.15),
					Pair(1,0.35),
					Pair(2,0.5)))
				"AU-0821" -> WeightedIntegerAmount(setOf(
					Pair(0,0.0),
					Pair(1,0.4),
					Pair(2,0.6)))
				"Horizon" -> WeightedIntegerAmount(setOf(
					Pair(0,0.3),
					Pair(1,0.4),
					Pair(2,0.4)))
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
