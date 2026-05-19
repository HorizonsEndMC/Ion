package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.PlayerDropItemTrigger.PlayerDropItemTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent

object PlayerDropItemTrigger : SequenceTriggerType<PlayerDropItemTriggerSettings>() {
	override fun setupChecks() {
		listen<PlayerDropItemEvent> { checkAllSequences(it.player, it) }
	}

	class PlayerDropItemTriggerSettings(
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == PlayerDropItemTrigger
		}
	}
}
