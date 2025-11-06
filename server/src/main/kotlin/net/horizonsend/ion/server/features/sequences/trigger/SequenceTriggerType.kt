package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import org.bukkit.entity.Player

abstract class SequenceTriggerType<T : SequenceTriggerType.TriggerSettings> {
	open fun setupChecks() {}

	fun checkAllSequences(player: Player) {
		for (sequenceKey in getCurrentSequences(player)) {
			checkPhaseTriggers(player, sequenceKey)
		}
	}

	protected fun checkPhaseTriggers(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>) {
		val currentPhase = SequenceManager.getCurrentPhase(player, sequenceKey)?.getValue() ?: return

		val triggerContext = TriggerContext(
			sequence = sequenceKey,
			callingTrigger = this,
			sequenceContext = SequenceManager.getSequenceData(player, sequenceKey).context,
			event = null
		)

		// Find all triggers for children on the current phase
		for (trigger in currentPhase.triggers) {
			if (!trigger.shouldProceed(player, triggerContext)) continue

			trigger.trigger(player)
			break
		}
	}

	abstract class TriggerSettings() {
		abstract fun shouldProceed(player: Player, context: TriggerContext): Boolean
	}

	override fun toString(): String {
		return javaClass.simpleName
	}
}
