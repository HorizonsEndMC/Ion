package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import org.bukkit.World
import java.util.function.Supplier

class DifficultyModule(
	controller: AIController,
	var internalDifficulty: Int = 2
) : AIModule(controller) {
	val isShieldAware get() = internalDifficulty >= 2

	val doBackOff get() = internalDifficulty >= 2

	val fleeChance: Double
		get() {
			return when (internalDifficulty) {
				0 -> 1.0
				1 -> 1.0
				2 -> 1.0
				3 -> 0.3
				4 -> 0.0
				else -> {
					1.0
				}
			}
		}

	val speedModifier: Double
		get() {
			return when (internalDifficulty) {
				0 -> 0.4
				1 -> 0.8
				2 -> 1.0
				3 -> 1.0
				4 -> 1.2
				else -> {
					1.0
				}
			}
		}

	val outOfRangeAggro: Double
		get() {
			return when (internalDifficulty) {
				0 -> 0.0
				1 -> 0.0
				2 -> 0.5
				3 -> 1.0
				4 -> 2.0
				else -> {
					0.0
				}
			}
		}

	val decayEmityThreshold: Double
		get() {
			return when (internalDifficulty) {
				0 -> 0.0
				1 -> 0.0
				2 -> 0.0
				3 -> 0.5
				4 -> 1.5
				else -> {
					0.0
				}
			}
		}

	val doNavigation: Boolean get() = internalDifficulty >= 2

	val shotVariation: Double
		get() {
			return when (internalDifficulty) {
				0 -> 1.0
				1 -> 0.5
				2 -> 0.25
				3 -> 0.10
				else -> 0.0
			}
		}

	val doubleEstimateAim get() = internalDifficulty >= 4

	/** crazy clickers vs trackpad users rip */
	val combatTickCooldownNanos: Long
		get() {
			return when (internalDifficulty) {
				0 -> 250
				1 -> 200
				2 -> 150
				3 -> 100
				4 -> 45
				else -> 150
		} * 1000000L
	}

	val actionPenalty : Long get() {
		return when (internalDifficulty) {
			0 -> 1000
				1 -> 500
				2 -> 250
				3 -> 100
				4 -> 0
				else -> 250
			} * 1000000L
		}

	val aimEverything get() = internalDifficulty >= 4

	val faceModifier get() = if (internalDifficulty <= 0) 0.5 else 1.0

	val aimAdjust: Double
		get() {
			return when (internalDifficulty) {
				0 -> 0.0
				1 -> 0.0
				2 -> 0.4
				3 -> 0.7
				4 -> 1.0
				else -> {
					0.0
				}
			}
		}

	val targetLowestShield get() = internalDifficulty >= 4

	val powerModeSwitch get() = internalDifficulty >= 2
	val useSpecialPowerModes get() = internalDifficulty >= 4

	val rewardMultiplier: Double
		get() {
			return when (internalDifficulty) {
				0 -> 0.7
				1 -> 0.9
				2 -> 1.0
				3 -> 1.25
				4 -> 1.5
				else -> 1.0
			}
		}


	companion object {
		val maxDifficulty = 4
		val minDifficulty = 0

		fun regularSpawnDifficultySupplier(world: World): Supplier<Int> = world.ion.configuration.aiDifficulty

		enum class AIDifficulty {
			EASY,
			NORMAL,
			HARD,
			BRUTAL,
			INSANE;
		}
	}
}
