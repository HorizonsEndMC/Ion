package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.steering.DistancePositioningModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class FleeMessageModule(
	controller: AIController,
	private val prefix: Component,
	private val messages: List<FleeTriggerMessage>,
) : AIModule(controller, true) {
	private val distanceModule = controller.getCoreModuleByType<DistancePositioningModule>()!!
	private val enmityModule = controller.getCoreModuleByType<EnmityModule>()!!
	private var fleeState: Boolean = false

	override fun tick() {
		if (!(distanceModule.isFleeing xor fleeState)) return
		fleeState = distanceModule.isFleeing
		val message = messages.filter { it.isFleeing == fleeState }.randomOrNull()

		for (opponent in enmityModule.enmityList) {
			val player = if (opponent.target is PlayerTarget) {
				(opponent.target as PlayerTarget).player
			} else {
				(opponent.target as? StarshipTarget)?.ship?.playerPilot
			} ?: continue
			message?.let { player.sendMessage(buildMessage(message.message)) }
		}
	}

	private fun buildMessage(msg: Component): Component = template(
		"{0}\n{1}: {2}",
		color = HEColorScheme.HE_MEDIUM_GRAY,
		paramColor = NamedTextColor.WHITE,
		useQuotesAroundObjects = true,
		prefix,
		starship.getDisplayName(),
		msg
	)
}


data class FleeTriggerMessage(
	val message: Component,
	val isFleeing: Boolean
)
