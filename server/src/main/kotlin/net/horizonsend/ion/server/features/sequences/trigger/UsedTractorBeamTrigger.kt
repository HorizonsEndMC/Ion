package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.event.multiblock.PlayerUseTractorBeamEvent
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import net.horizonsend.ion.server.features.sequences.trigger.UsedTractorBeamTrigger.TractorBeamTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object UsedTractorBeamTrigger : SequenceTriggerType<TractorBeamTriggerSettings>() {
	override fun setupChecks() {
		listen<PlayerUseTractorBeamEvent> { for (sequenceKey in getCurrentSequences(it.player)) { checkPhaseTriggers(it.player, sequenceKey) } }
	}

	class TractorBeamTriggerSettings(
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>): Boolean {
			return callingTrigger == UsedTractorBeamTrigger
		}
	}
}
