package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.StarshipReleaseTrigger.StarshipReleaseTriggerSettings
import net.horizonsend.ion.server.features.starship.event.StarshipReleaseEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object StarshipReleaseTrigger : SequenceTriggerType<StarshipReleaseTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipReleaseEvent> {
			val player = it.starship.playerPilot ?: return@listen
			checkAllSequences(player, it)
		}
	}

	class StarshipReleaseTriggerSettings() : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == StarshipReleaseTrigger
		}
	}
}
