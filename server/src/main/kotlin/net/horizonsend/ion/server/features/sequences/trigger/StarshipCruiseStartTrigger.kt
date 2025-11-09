package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import net.horizonsend.ion.server.features.sequences.trigger.StarshipCruiseStartTrigger.StartCruseTriggerSettings
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStartCruisingEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object StarshipCruiseStartTrigger : SequenceTriggerType<StartCruseTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipStartCruisingEvent> {
			val player = it.starship.playerPilot ?: return@listen
			for (sequenceKey in getCurrentSequences(player)) { checkPhaseTriggers(player, sequenceKey, it) }
		}
	}

	class StartCruseTriggerSettings() : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == StarshipCruiseStartTrigger
		}
	}
}
