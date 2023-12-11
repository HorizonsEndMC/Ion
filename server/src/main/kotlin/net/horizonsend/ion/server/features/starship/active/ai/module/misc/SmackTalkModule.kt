package net.horizonsend.ion.server.features.starship.active.ai.module.misc

import net.horizonsend.ion.server.features.starship.active.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import kotlin.random.Random

class SmackTalkModule(
	controller: AIController,
	private vararg val messages: Component
) : AIModule(controller) {
	var tickChance: Double = 0.8
	var tickThreshold= 600
	var ticks = 0
	var sendRange = 500.0

	override fun tick() {
		if (Random.nextDouble() >= tickChance) ticks++

		if (ticks >= tickThreshold) {
			ticks = 0
			sendMessage()
		}
	}

	fun sendMessage() {
		val message = messages.randomOrNull() ?: return

		val players = world.getNearbyPlayers(getCenter(), sendRange)

		for (player in players) {
			player.sendMessage(message)
		}
	}
}
