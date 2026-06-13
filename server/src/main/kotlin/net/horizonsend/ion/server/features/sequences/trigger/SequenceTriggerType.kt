package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import org.bukkit.entity.Player
import org.bukkit.event.Event

abstract class SequenceTriggerType<T : SequenceTriggerType.TriggerSettings> {
	open fun setupChecks() {}

	fun checkAllSequences(player: Player, event: Event?) {
		for (sequenceKey in getCurrentSequences(player)) {
			checkPhaseTriggers(player, sequenceKey, event)
		}
	}

	/**
	 * Checks if this type, with the trigger set up as the one passed in, matches the matchAgainst value for checks
	 **/
	open fun matches(trigger: SequenceTrigger<*>, matchAgainst: SequenceTriggerType<*>): Boolean = this == matchAgainst

	protected fun checkPhaseTriggers(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, event: Event?) {
		val currentPhase = SequenceManager.getCurrentPhase(player, sequenceKey)?.getValue() ?: return

		if (currentPhase.triggers.none { it.type.matches(it, this) }) return

		val triggerContext = TriggerContext(
			sequence = sequenceKey,
			callingTrigger = this,
			sequenceContext = SequenceManager.getSequenceData(player, sequenceKey).context,
			event = event
		)

		// Find all triggers for children on the current phase
		for (trigger in currentPhase.triggers) {
			if (!trigger.shouldProceed(player, triggerContext)) continue

			trigger.trigger(player, triggerContext)
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
