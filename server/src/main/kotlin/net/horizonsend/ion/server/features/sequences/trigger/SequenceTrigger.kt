package net.horizonsend.ion.server.features.sequences.trigger

import org.bukkit.entity.Player

class SequenceTrigger<T : SequenceTriggerType.TriggerSettings>(val type: SequenceTriggerType<T>, private val settings: T) {
	private var onTriggered: SequenceTrigger<T>.(Player) -> Unit = {}

	fun setTriggerResult(result: SequenceTrigger<T>.(Player) -> Unit) {
		onTriggered = result
	}

	fun trigger(player: Player) {
		onTriggered.invoke(this, player)
	}

	fun shouldProceed(player: Player, sequenceKey: String, callingTrigger: SequenceTriggerType<*>): Boolean {
		return settings.shouldProceed(player, sequenceKey, callingTrigger)
	}
}
