package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import net.horizonsend.ion.server.features.sequences.trigger.StarshipCruiseTrigger.CruseTriggerSettings
import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object StarshipCruiseTrigger : SequenceTriggerType<CruseTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipTranslateEvent> {
			val player = it.ship.playerPilot ?: return@listen
			if (it.movement.source != TranslateMovement.MovementSource.CRUISE) return@listen
			for (sequenceKey in getCurrentSequences(player)) { checkPhaseTriggers(player, sequenceKey) }
		}
	}

	class CruseTriggerSettings(
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<net.horizonsend.ion.server.features.sequences.Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>): Boolean {
			return callingTrigger == StarshipCruiseTrigger
		}
	}
}
