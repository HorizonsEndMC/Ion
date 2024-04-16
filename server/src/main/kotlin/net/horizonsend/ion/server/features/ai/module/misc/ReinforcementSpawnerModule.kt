package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.spawner.ReinforcementSpawner
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component

class ReinforcementSpawnerModule(
	controller: AIController,
	val spawner: ReinforcementSpawner,
	private val activationAverageShieldHealth: Double,
	private val spawnBroadCastMessage: Component?,
	val delay: Long = 200,
) : AIModule(controller) {
	private var triggered: Boolean = false

	override fun tick() {
		if (controller.getAverageShieldHealth() > activationAverageShieldHealth || triggered) return

		triggered = true
		Tasks.syncDelay(120) {
			spawner.trigger(log, AISpawningManager.context)
			sendMessage()
		}
	}

	fun sendMessage() {
		if (spawnBroadCastMessage == null) return

		val players = getCenter().toLocation(world).getNearbyPlayers(500.0)

		val (x, y, z) = starship.centerOfMass

		val formatted = template(
			message = spawnBroadCastMessage,
			paramColor = HEColorScheme.HE_LIGHT_GRAY,
			useQuotesAroundObjects = false,
			controller.getPilotName(),
			x,
			y,
			z,
			world.name,
			starship.getDisplayName()
		)

		for (player in players) player.sendMessage(formatted)
	}
}
