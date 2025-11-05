package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceContext
import net.horizonsend.ion.server.features.sequences.trigger.CombinedOrTrigger.CombinedOrTriggerSettings
import org.bukkit.entity.Player

object CombinedOrTrigger : SequenceTriggerType<CombinedOrTriggerSettings>() {
	override fun setupChecks() {}

	class CombinedOrTriggerSettings(
		val children: Collection<SequenceTrigger<*>>
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>, context: SequenceContext): Boolean {
			return children.any { trigger -> trigger.shouldProceed(player, sequenceKey, callingTrigger, context) }
		}
	}
}
