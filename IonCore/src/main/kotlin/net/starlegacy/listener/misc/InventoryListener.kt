package net.starlegacy.listener.misc

import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.LegacyItemUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType

object InventoryListener : SLEventListener() {
	@EventHandler
	fun contrabandClick(event: InventoryClickEvent) {
		if (LegacyItemUtils.isContraband(event.currentItem)) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun contrabandClick(event: InventoryMoveItemEvent) {
		if (LegacyItemUtils.isContraband(event.item)) {
			event.isCancelled = true
		}
	}

	/** allow players to put items in furnace fuel slots */
	@EventHandler
	fun onFuelClick(e: InventoryClickEvent) {
		val inv = e.clickedInventory
		if (e.slot != 1 || inv == null || inv.type != InventoryType.FURNACE) {
			return
		}

		val cursor = e.cursor
		val clickedItem = inv.getItem(e.slot)
		when (e.click) {
			ClickType.LEFT -> when {
				cursor != null && clickedItem == null -> {
					inv.setItem(e.slot, cursor.clone())
					cursor.amount = 0
					e.isCancelled = true
				}

				cursor == null && clickedItem != null -> {
					e.cursor = clickedItem
					e.currentItem = null
				}

				cursor != null && cursor.isSimilar(clickedItem) -> {
					val amount = clickedItem!!.amount

					if (amount >= clickedItem.maxStackSize) {
						return
					}

					val cursorAmount = cursor.amount
					val addition = Math.min(clickedItem.maxStackSize - amount, cursorAmount)
					clickedItem.amount = amount + addition
					cursor.amount = cursorAmount - addition
					e.isCancelled = true
				}
			}

			ClickType.RIGHT -> if (cursor != null) when {
				clickedItem == null -> {
					val item = cursor.clone()
					item.amount = 1
					inv.setItem(e.slot, item)
					cursor.amount = cursor.amount - 1
					e.isCancelled = true
				}

				cursor.isSimilar(clickedItem) -> {
					val amount = clickedItem.amount

					if (amount >= clickedItem.maxStackSize) {
						return
					}

					val cursorAmount = cursor.amount
					val addition = Math.min(1, cursorAmount)
					clickedItem.amount = amount + addition
					cursor.amount = cursorAmount - addition
					e.isCancelled = true
				}
			}

			else -> return
		}
	}
}
