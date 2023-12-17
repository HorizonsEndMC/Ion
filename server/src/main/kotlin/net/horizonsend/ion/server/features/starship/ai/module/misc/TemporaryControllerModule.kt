package net.horizonsend.ion.server.features.starship.ai.module.misc

import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class TemporaryControllerModule(
	controller: AIController,
	private val previousController: AIController,
	private val minTimeSinceLastDamage: Int = 1000 * 60 * 1,
) : AIModule(controller) {
	override fun tick() {
		if (shouldReturn()) returnToPreviousController()
	}

	private fun shouldReturn(): Boolean {
		val timeCutoff = System.currentTimeMillis() - minTimeSinceLastDamage

		val damagers = starship.damagers.filter { it.value.lastDamaged > timeCutoff }

		return damagers.isEmpty()
	}

	private fun returnToPreviousController() {
		val previousController = previousController
		starship.controller = previousController
	}
}
