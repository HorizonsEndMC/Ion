package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import net.horizonsend.ion.server.features.sequences.trigger.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object PlayerInteractTrigger : SequenceTriggerType<InteractTriggerSettings>() {
	override fun setupChecks() {
		listen<PlayerInteractEvent> { for (sequenceKey in getCurrentSequences(it.player)) { checkPhaseTriggers(it.player, sequenceKey) } }
	}

	class InteractTriggerSettings(
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>): Boolean {
			return callingTrigger == PlayerInteractTrigger
		}
	}
}
