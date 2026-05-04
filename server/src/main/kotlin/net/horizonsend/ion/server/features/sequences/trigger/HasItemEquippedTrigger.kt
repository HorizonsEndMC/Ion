package net.horizonsend.ion.server.features.sequences.trigger

import io.papermc.paper.event.packet.ClientTickEndEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object HasItemEquippedTrigger : SequenceTriggerType<HasItemEquippedTrigger.HasItemEquippedTriggerSettings>() {
	override fun setupChecks() {
		// Check every time the client is ticked
		listen<ClientTickEndEvent> { checkAllSequences(it.player, it) }
	}

	class HasItemEquippedTriggerSettings(private val itemPredicate: (ItemStack?) -> Boolean) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return player.inventory.armorContents.any(itemPredicate)
		}
	}
}
