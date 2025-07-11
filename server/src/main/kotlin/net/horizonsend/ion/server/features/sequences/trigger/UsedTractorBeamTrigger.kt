package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.event.multiblock.PlayerUseTractorBeamEvent
import net.horizonsend.ion.server.features.sequences.trigger.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player

object UsedTractorBeamTrigger : SequenceTriggerType<InteractTriggerSettings>() {
	override fun setupChecks() {
		listen<PlayerUseTractorBeamEvent> { checkPhaseTriggers(it.player) }
	}

	class InteractTriggerSettings(
	) : TriggerSettings() {
		override fun shouldProceed(player: Player): Boolean {
			return true
		}
	}
}
