package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.PlayerInteractTrigger.PlayerInteractTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object PlayerInteractTrigger : SequenceTriggerType<PlayerInteractTriggerSettings>() {
	override fun setupChecks() {
		listen<PlayerInteractEvent> { checkAllSequences(it.player, it) }
	}

	class PlayerInteractTriggerSettings(
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == PlayerInteractTrigger
		}
	}
}
