package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class DespawnModule(
	controller: AIController,
	val condition: DespawnModule.() -> Boolean
) : AIModule(controller) {

	fun evaluateDespawn(): Boolean {
		return condition.invoke(this)
	}

	companion object {

		val neverDespawn: DespawnModule.() -> Boolean = {
			false
		}
	}
}
