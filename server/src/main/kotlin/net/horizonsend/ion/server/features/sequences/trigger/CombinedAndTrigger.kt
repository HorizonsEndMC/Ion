package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.CombinedAndTrigger.CombinedAndTriggerSettings
import org.bukkit.entity.Player

object CombinedAndTrigger : SequenceTriggerType<CombinedAndTriggerSettings>() {
	override fun setupChecks() {}

	class CombinedAndTriggerSettings(
		val children: Collection<SequenceTrigger<*>>
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, callingTrigger: SequenceTriggerType<*>): Boolean {
			return children.all { trigger -> trigger.shouldProceed(player, callingTrigger) }
		}
	}
}
