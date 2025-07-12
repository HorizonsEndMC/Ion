package net.horizonsend.ion.server.features.sequences.trigger

import io.papermc.paper.event.packet.ClientTickEndEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ContainsItemTrigger : SequenceTriggerType<ContainsItemTrigger.ContainsItemTriggerSettings>() {
	override fun setupChecks() {
		// Check every time the client is ticked
		listen<ClientTickEndEvent> { checkPhaseTriggers(it.player) }
	}

	class ContainsItemTriggerSettings(private val itemPredicate: (ItemStack?) -> Boolean) : TriggerSettings() {
		override fun shouldProceed(player: Player, callingTrigger: SequenceTriggerType<*>): Boolean {
			return player.inventory.contents.any(itemPredicate)
		}
	}
}
