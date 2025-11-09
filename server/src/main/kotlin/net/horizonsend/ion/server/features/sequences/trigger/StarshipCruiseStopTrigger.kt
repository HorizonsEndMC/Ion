package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import net.horizonsend.ion.server.features.sequences.trigger.StarshipCruiseStopTrigger.StopCruseTriggerSettings
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStopCruisingEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object StarshipCruiseStopTrigger : SequenceTriggerType<StopCruseTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipStopCruisingEvent> {
			val player = it.starship.playerPilot ?: return@listen
			for (sequenceKey in getCurrentSequences(player)) { checkPhaseTriggers(player, sequenceKey, it) }
		}
	}

	class StopCruseTriggerSettings() : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == StarshipCruiseStopTrigger
		}
	}
}
