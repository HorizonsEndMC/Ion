package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class DifficultyModule(
	controller: AIController,
	private var internalDifficulty: Int = 3
) : AIModule(controller){

	val isShieldAware get() = internalDifficulty >= 3
	val doBackOff get() = internalDifficulty >= 3
	val speedDebuff = internalDifficulty <= 1
	val shotVariation : Double get() {
		return when (internalDifficulty) {
			0,1 -> 0.3
			2 -> 0.15
			else -> 0.0
		}
	}
	val faceModifier get() = if (internalDifficulty <= 1) 0.5 else 1.0

	fun setDifficulty(i :Int) {
		internalDifficulty = i
	}
}
