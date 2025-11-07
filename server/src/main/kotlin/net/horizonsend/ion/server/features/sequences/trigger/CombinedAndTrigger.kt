package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.CombinedAndTrigger.CombinedAndTriggerSettings
import org.bukkit.entity.Player

object CombinedAndTrigger : SequenceTriggerType<CombinedAndTriggerSettings>() {
	override fun setupChecks() {}

	override fun matches(trigger: SequenceTrigger<*>, matchAgainst: SequenceTriggerType<*>): Boolean {
		val settings = trigger.settings as? CombinedAndTriggerSettings
		return settings?.children?.any { it.type.matches(it, matchAgainst) } ?: false
	}

	class CombinedAndTriggerSettings(
		vararg val children: SequenceTrigger<*>
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return children.all { trigger -> trigger.shouldProceed(player, context) }
		}
	}
}
