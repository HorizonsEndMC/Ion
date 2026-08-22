package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.player.CombatTimer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

@CommandAlias("disposal|dispose|trash|etrash")
object DisposalCommand : SLCommand(), Listener {

	private const val INVENTORY_SIZE = 36
	private val TITLE = Component.text("Disposal")
	private fun isCombatTagged(player: Player): Boolean {
		return CombatTimer.isNpcCombatTagged(player) ||
			CombatTimer.isPvpCombatTagged(player)
	}
	@EventHandler
	fun onInventoryClose(event: InventoryCloseEvent) {

		val player = event.player as? Player ?: return
		if (event.inventory.holder !is DisposalHolder) {
			return
		}
		// If the player became combat tagged while the disposal inventory was open, return their items (main reason why we needed to recreate the /disposal command)
		if (isCombatTagged(player)) {
			returnItems(player, event.inventory)
			player.userError("You became combat tagged. Your items were returned.")
			return
		}

		event.inventory.clear()
	}

	@Default
	fun onDisposal(sender: Player) {
		if (isCombatTagged(sender)) {
			sender.userError("You cannot dispose of items while combat tagged.")
			return
		}
		val inventory = Bukkit.createInventory(
			DisposalHolder(),
			INVENTORY_SIZE,
			TITLE
		)
		sender.openInventory(inventory)
	}

	private fun returnItems(player: Player, inventory: Inventory) {
		for (item in inventory.contents) {
			if (item == null || item.isEmpty) {
				continue
			}
			val remainderItems = player.inventory.addItem(item)
			//If the players inventory became full while the gui was open, drops remaining items  on the ground
			for (remainderItems in remainderItems.values) {
				player.world.dropItemNaturally(player.location, remainderItems)
			}
		}
		inventory.clear()
	}

	private class DisposalHolder : InventoryHolder {
		override fun getInventory(): Inventory {
			throw UnsupportedOperationException(
				"DisposalHolder does not own an inventory directly."
			)
		}
	}
}


