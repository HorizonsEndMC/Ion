package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.PlayerChangedWorldTrigger.PlayerChangedWorldTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent

object PlayerChangedWorldTrigger : SequenceTriggerType<PlayerChangedWorldTriggerSettings>() {
	override fun setupChecks() {
		listen<PlayerChangedWorldEvent> {
			checkAllSequences(it.player, it)
		}
	}

	class PlayerChangedWorldTriggerSettings(val predicate: (Player, TriggerContext) -> Boolean = { _, _ -> true }) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return predicate.invoke(player, context)
		}
	}
}
