package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceContext
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.phases.SequencePhase
import org.bukkit.entity.Player

class SequenceTrigger<T : SequenceTriggerType.TriggerSettings>(val type: SequenceTriggerType<T>, private val settings: T, private var triggerResult: (Player) -> Unit) {
	fun trigger(player: Player) {
		triggerResult.invoke(player)
	}

	fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>, context: SequenceContext): Boolean {
		return settings.shouldProceed(player, sequenceKey, callingTrigger, context)
	}

	companion object {
		fun startPhase(phaseKey: IonRegistryKey<SequencePhase, out SequencePhase>): (Player) -> Unit = {
			val phase = phaseKey.getValue()
			SequenceManager.startPhase(it, phase.sequenceKey, phase.phaseKey)
		}
	}
}
