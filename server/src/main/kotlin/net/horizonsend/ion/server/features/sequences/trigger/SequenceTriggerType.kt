package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.SequenceManager
import org.bukkit.entity.Player

abstract class SequenceTriggerType<T : SequenceTriggerType.TriggerSettings> {
	open fun setupChecks() {}

	protected fun checkPhaseTriggers(player: Player) {
		val currentPhase = SequenceManager.getCurrentPhase(player) ?: return

		// Find all triggers for children on the current phase
		for (trigger in currentPhase.danglingTriggers) {
			if (!trigger.shouldProceed(player)) continue

			trigger.trigger(player)
			break
		}
	}

	abstract class TriggerSettings() {
		abstract fun shouldProceed(player: Player): Boolean
	}
}
