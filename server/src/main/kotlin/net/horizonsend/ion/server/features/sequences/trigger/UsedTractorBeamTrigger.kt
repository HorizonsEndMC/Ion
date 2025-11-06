package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.event.multiblock.PlayerUseTractorBeamEvent
import net.horizonsend.ion.server.features.sequences.trigger.UsedTractorBeamTrigger.TractorBeamTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object UsedTractorBeamTrigger : SequenceTriggerType<TractorBeamTriggerSettings>() {
	override fun setupChecks() = listen<PlayerUseTractorBeamEvent> { checkAllSequences(it.player, it) }

	class TractorBeamTriggerSettings() : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == UsedTractorBeamTrigger
		}
	}
}
