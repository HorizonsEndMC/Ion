package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object PlayerInteractTrigger : SequenceTriggerType<InteractTriggerSettings>() {
	override fun setupChecks() {
		listen<PlayerInteractEvent> { checkAllSequences(it.player) }
	}

	class InteractTriggerSettings(
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == PlayerInteractTrigger
		}
	}
}
