package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.features.transport.items.util.addToFurnace
import net.horizonsend.ion.server.features.transport.items.util.getSpecialFurnaceInputSlot
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.SHELF_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import org.bukkit.block.BlockType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.min

object InventoryListener : SLEventListener() {
	@EventHandler(priority = EventPriority.LOWEST)
	fun contrabandClick(event: InventoryClickEvent) {
		if (LegacyItemUtils.isContraband(event.currentItem)) {
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun contrabandClick(event: InventoryMoveItemEvent) {
		if (LegacyItemUtils.isContraband(event.item)) {
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	fun routeSpecialFurnaceInput(event: InventoryMoveItemEvent) {
		val destination = event.destination as? FurnaceInventory ?: return
		if (getSpecialFurnaceInputSlot(destination, event.item) == null) return

		event.isCancelled = true

		val movedItem = event.item.clone()
		val remaining = addToFurnace(destination, movedItem)
		val movedAmount = movedItem.amount - remaining
		if (movedAmount <= 0) return

		movedItem.amount = movedAmount
		event.source.removeItem(movedItem)
	}

	// Decorated pots can be extracted from and have items moved into chests.
	@EventHandler(priority = EventPriority.LOWEST)
	fun contrabandPotClick(event: PlayerInteractEvent) {
		if (
			event.item?.type?.isShulkerBox ?: false &&
			event.action == Action.RIGHT_CLICK_BLOCK &&
			event.clickedBlock?.type?.asBlockType() == BlockType.DECORATED_POT
			) event.isCancelled = true

	}

	private fun isShelf(blockType: BlockType?): Boolean {

		// this shouldn't be ble to fail, so i will unwisely assume the null can never be null
		val shelfTypes = mutableListOf<BlockType>()
		SHELF_TYPES.forEach { shelfTypes.add(it.asBlockType() ?: BlockType.AIR) }

		return blockType in shelfTypes
	}

	// Shelves can have illegal items stored in them.
	// this almost completely preserves shelf functionality
	@EventHandler(priority = EventPriority.LOWEST)
	fun contrabandShelfClick(event: PlayerInteractEvent) {
		if (
			(0..8).any { event.player.inventory.getItem(it)?.type?.isShulkerBox ?: false } &&
			event.action == Action.RIGHT_CLICK_BLOCK &&
			isShelf(event.clickedBlock?.type?.asBlockType())
		) event.isCancelled = true
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
					e.setCursor(clickedItem)
					e.currentItem = null
				}

				cursor != null && cursor.isSimilar(clickedItem) -> {
					val amount = clickedItem!!.amount

					if (amount >= clickedItem.maxStackSize) {
						return
					}

					val cursorAmount = cursor.amount
					val addition = min(clickedItem.maxStackSize - amount, cursorAmount)
					clickedItem.amount = amount + addition
					cursor.amount = cursorAmount - addition
					e.isCancelled = true
				}
			}

			ClickType.RIGHT -> if (cursor != null) {
				when {
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
						val addition = min(1, cursorAmount)
						clickedItem.amount = amount + addition
						cursor.amount = cursorAmount - addition
						e.isCancelled = true
					}
				}
			}

			else -> return
		}
	}
}
