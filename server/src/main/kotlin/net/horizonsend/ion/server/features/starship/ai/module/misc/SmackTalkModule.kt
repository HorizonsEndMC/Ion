package net.horizonsend.ion.server.features.starship.ai.module.misc

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import kotlin.random.Random

class SmackTalkModule(
	controller: AIController,
	private val prefix: Component,
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

		val text = template(
			message = "{0}\n{1}: {2}",
			color = HEColorScheme.HE_MEDIUM_GRAY,
			paramColor = WHITE,
			useQuotesAroundObjects = true,
			prefix,
			starship.getDisplayName(),
			message
		)

		for (player in players) player.sendMessage(text)
	}
}
