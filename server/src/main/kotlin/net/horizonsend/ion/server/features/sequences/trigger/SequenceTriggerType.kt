package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceManager
import org.bukkit.entity.Player

abstract class SequenceTriggerType<T : SequenceTriggerType.TriggerSettings> {
	open fun setupChecks() {}

	protected fun checkPhaseTriggers(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>) {
		val currentPhase = SequenceManager.getCurrentPhase(player, sequenceKey)?.getValue() ?: return

		// Find all triggers for children on the current phase
		for (trigger in currentPhase.danglingTriggers) {
			if (!trigger.shouldProceed(player, sequenceKey, this@SequenceTriggerType)) continue

			trigger.trigger(player)
			break
		}
	}

	abstract class TriggerSettings() {
		abstract fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>): Boolean
	}

	override fun toString(): String {
		return javaClass.simpleName
	}
}
