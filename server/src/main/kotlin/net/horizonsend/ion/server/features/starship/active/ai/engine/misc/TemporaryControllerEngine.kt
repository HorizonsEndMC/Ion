package net.horizonsend.ion.server.features.starship.active.ai.engine.misc

import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class TemporaryControllerEngine(
	controller: AIController,
	private val previousController: AIController,
	private val minTimeSinceLastDamage: Int = 1000 * 60 * 1,
) : AIEngine(controller) {
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
