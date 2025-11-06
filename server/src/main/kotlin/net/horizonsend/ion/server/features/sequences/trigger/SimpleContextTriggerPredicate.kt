package net.horizonsend.ion.server.features.sequences.trigger

import org.bukkit.entity.Player

class SimpleContextTriggerPredicate(private vararg val predicates: (Player, TriggerContext) -> Boolean) : SequenceTriggerType.TriggerSettings() {
	override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
		if (predicates.isEmpty()) return true
		return predicates.all { it.invoke(player, context) }
	}
}
