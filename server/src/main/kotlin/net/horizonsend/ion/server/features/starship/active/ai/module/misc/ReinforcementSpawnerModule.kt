package net.horizonsend.ion.server.features.starship.active.ai.module.misc

import net.horizonsend.ion.server.features.starship.active.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component

class ReinforcementSpawnerModule(
	controller: AIController,
	vararg spawners: AISpawner,
	val minAverageShieldHealth: Double,
	val engagementStartMessage: Component,
	val spawnBroadCastMessage: Component,
) : AIModule(controller) {
	//TODO
}
