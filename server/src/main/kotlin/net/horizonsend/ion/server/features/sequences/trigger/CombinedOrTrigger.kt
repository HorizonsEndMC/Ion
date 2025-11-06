package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.CombinedOrTrigger.CombinedOrTriggerSettings
import org.bukkit.entity.Player

object CombinedOrTrigger : SequenceTriggerType<CombinedOrTriggerSettings>() {
	override fun setupChecks() {}

	class CombinedOrTriggerSettings(
		val children: Collection<SequenceTrigger<*>>
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return children.any { trigger -> trigger.shouldProceed(player, context) }
		}
	}
}
