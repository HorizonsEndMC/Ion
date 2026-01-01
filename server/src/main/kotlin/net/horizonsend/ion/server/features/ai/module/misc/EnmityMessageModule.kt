package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.ai.configuration.AIEmities
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class EnmityMessageModule(
	controller: AIController,
	private val prefix: Component,
	private val messages: List<EnmityTriggerMessage>,
	private val configSupplier: () -> AIEmities.AIEmityConfiguration
) : AIModule(controller, true) {
	private val enmityModule = controller.getCoreModuleByType<EnmityModule>()!!
	private val messaged = mutableSetOf<Pair<AITarget, String>>() // (AITarget, message ID)

	override fun tick() {
		val enmity = enmityModule
		val config = configSupplier()

		for (opponent in enmity.enmityList) {
			for (msg in messages) {
				val key = opponent.target to msg.id
				if (messaged.contains(key)) continue
				if (!msg.shouldTrigger(opponent, config)) continue

				val player = if (opponent.target is PlayerTarget) {
					(opponent.target as PlayerTarget).player
				} else {
					(opponent.target as? StarshipTarget)?.ship?.playerPilot
				} ?: continue
				player.sendMessage(buildMessage(msg.message))
				messaged.add(key)
			}
		}
		messaged.removeIf { messaged ->
			!enmityModule.enmityList.any {
				it.target == messaged.first
			}
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

	companion object {
		val triggeredByFriendlyFire = { opponent: EnmityModule.AIOpponent, config: AIEmities.AIEmityConfiguration ->
			opponent.damagerWeight > 0.1
				&& opponent.baseWeight <= opponent.damagerWeight && !opponent.aggroed
		}

		val escalatedFriendlyFire = { opponent: EnmityModule.AIOpponent, config: AIEmities.AIEmityConfiguration ->
			opponent.damagerWeight > (config.initialAggroThreshold * 2)
				&& opponent.baseWeight <= opponent.damagerWeight && !opponent.aggroed
		}

		val betrayalAggro = { opponent: EnmityModule.AIOpponent, config: AIEmities.AIEmityConfiguration ->
			opponent.damagerWeight > (config.initialAggroThreshold * 2)
				&& opponent.baseWeight < config.initialAggroThreshold
		}
	}
}


data class EnmityTriggerMessage(
	val id: String,
	val message: Component,
	val shouldTrigger: (opponent: EnmityModule.AIOpponent, config: AIEmities.AIEmityConfiguration) -> Boolean
)
