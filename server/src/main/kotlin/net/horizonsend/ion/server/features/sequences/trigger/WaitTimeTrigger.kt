package net.horizonsend.ion.server.features.sequences.trigger

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceContext
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import kotlin.jvm.optionals.getOrNull

object WaitTimeTrigger : SequenceTriggerType<WaitTimeTrigger.WaitTimeTriggerSettings>() {
	override fun setupChecks() {
		listen<ServerTickEndEvent> {
			for (player in SequenceManager.allPlayers()) {
				for (sequenceKey in getCurrentSequences(player)) {
					checkPhaseTriggers(player, sequenceKey)
				}
			}
		}
	}

	class WaitTimeTriggerSettings(val storageKey: String, val triggerDelayMillis: Long) : TriggerSettings() {
		override fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>, context: SequenceContext): Boolean {
			val triggerTime = SequenceManager.getSequenceData(player, sequenceKey).get<Long>(storageKey).getOrNull() ?: return false
			return (triggerTime + triggerDelayMillis) <= System.currentTimeMillis()
		}
	}
}
