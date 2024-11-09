package net.horizonsend.ion.server.features.custom

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.server.command.admin.ConvertCommand
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.player.NewPlayerProtection.updateProtection
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

object ItemConverters : SLEventListener() {
	fun handleFurnaceInventory(inventory: FurnaceInventory) {
		inventory.smelting?.let { handleBrokenName(it) }
		inventory.fuel?.let { handleBrokenName(it) }
		inventory.result?.let { handleBrokenName(it) }
	}

	@EventHandler
	@Suppress("Unused")
	fun onInventoryOpen(event: InventoryOpenEvent) {
		val inventory = event.inventory
		for (item in inventory.filterNotNull()) {
			handleBrokenName(item)
			val newVersion = ConvertCommand.convertCustomMineral(item)

			if (newVersion != null) {
				item.type = newVersion.type
				item.itemMeta = newVersion.itemMeta
				item.amount = newVersion.amount
			}

			val possibleDrill = ConvertCommand.tryConvertDrill(item)

			if (possibleDrill != null) {
				item.type = possibleDrill.type
				item.itemMeta = possibleDrill.itemMeta
				item.amount = possibleDrill.amount
			}
		}
	}

	@EventHandler
	fun onPlayerLogin(event: PlayerJoinEvent) {
		event.player.updateProtection()

		val inventory = event.player.inventory
		for (item in inventory.filterNotNull()) {
			handleBrokenName(item)
			val newVersion = ConvertCommand.convertCustomMineral(item)

			if (newVersion != null) {
				item.type = newVersion.type
				item.itemMeta = newVersion.itemMeta
				item.amount = newVersion.amount

				event.player.updateInventory()
			}

			val possibleDrill = ConvertCommand.tryConvertDrill(item)

			if (possibleDrill != null) {
				item.type = possibleDrill.type
				item.itemMeta = possibleDrill.itemMeta
				item.amount = possibleDrill.amount

				event.player.updateInventory()
			}
		}
	}

	fun handleBrokenName(item: ItemStack) {
		val custom = item.customItem ?: return
		val idealName = custom.constructItemStack().itemMeta.displayName() ?: return
		val displayName = item.itemMeta.displayName() ?: return
		if (idealName == displayName) return
		if (displayName.children().isNotEmpty()) return // New names
		if (displayName.decorations()[ITALIC] != TextDecoration.State.FALSE) return

		item.updateMeta { it.displayName(idealName) }
	}
}
