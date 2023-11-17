package net.horizonsend.ion.server.features.starship.active.ai.engine.misc

import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component

class ReinforcementSpawnerEngine(
	controller: AIController,
	vararg spawners: AISpawner,
	val minAverageShieldHealth: Double,
	val engagementStartMessage: Component,
	val spawnBroadCastMessage: Component,
) : AIEngine(controller) {
	//TODO
}
