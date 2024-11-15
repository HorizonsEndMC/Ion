package net.horizonsend.ion.server.features.gui.interactable

import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.UUID

abstract class InteractableGUI(val viewer: Player) : InventoryHolder {
	protected abstract val internalInventory: Inventory
	abstract val inventorySize: Int

	fun open() {
		// Will return CRAFTING if none is open
		if (viewer.openInventory.type != InventoryType.CRAFTING && viewer.openInventory.type != InventoryType.CREATIVE) return

		val view = viewer.openInventory(inventory) ?: return

		setup(view)
	}

	abstract fun setup(view: InventoryView)

	abstract fun handleClose(event: InventoryCloseEvent)

	abstract fun handleAddItem(slot: Int, item: ItemStack, event: InventoryInteractEvent)

	abstract fun handleRemoveItem(slot: Int, event: InventoryClickEvent)

	abstract fun handleSwapItem(slot: Int, currentItem: ItemStack, new: ItemStack, event: InventoryClickEvent)

	/**
	 * Events from the player's clicked inventory while the mod menu is on top
	 **/
	fun handlePlayerClick(event: InventoryClickEvent) {
		val currentItem = event.currentItem
		val cursorItem = event.cursor
		val index = event.slot

		if (event.click != ClickType.SHIFT_LEFT && event.click != ClickType.SHIFT_RIGHT) return

		when {
			currentItem != null && cursorItem.type == Material.AIR -> handleAddItem(index, currentItem, event)
			currentItem == null && cursorItem.type != Material.AIR -> handleRemoveItem(index, event)
			else -> event.isCancelled = true
		}
	}

	/**
	 * Events from the player's clicked mod menu
	 **/
	fun handleClick(event: InventoryClickEvent) {
		val currentItem = event.currentItem
		val cursorItem = event.cursor
		val index = event.slot

		if (event.isCancelled) return

		when {
			event.click == ClickType.NUMBER_KEY -> handleNumberKey(index, currentItem, event)
			currentItem == null && cursorItem.type != Material.AIR -> handleAddItem(index, cursorItem, event)
			currentItem != null && cursorItem.type == Material.AIR -> handleRemoveItem(index, event)
			currentItem != null && cursorItem.type != Material.AIR -> handleSwapItem(index, currentItem, cursorItem, event)
			else -> event.isCancelled = true
		}
	}

	fun handleDrag(event: InventoryDragEvent) {
		// If dragging something outside the inventory
		if (event.rawSlots.none { it < inventorySize }) return

			when {
			// Unstackable item can't be split across multiple slots
			event.cursor == null && event.oldCursor.maxStackSize == 1 -> {
				// Will only end up in a single slot
				val slot = event.rawSlots.first()
				handleAddItem(slot, event.newItems[slot]!!, event)
			}
			else -> event.isCancelled = true
		}
	}

	private fun handleNumberKey(slotNumber: Int, currentItem: ItemStack?, event: InventoryClickEvent) {
		val playerItem = viewer.inventory.getItem(event.hotbarButton) ?: ItemStack(Material.AIR)

		when {
			currentItem == null && playerItem.type != Material.AIR -> handleAddItem(slotNumber, playerItem, event)
			currentItem != null && playerItem.type == Material.AIR -> handleRemoveItem(slotNumber, event)
			currentItem != null && playerItem.type != Material.AIR -> handleSwapItem(slotNumber, currentItem, playerItem, event)
			else -> event.isCancelled = true
		}
	}

	companion object : SLEventListener() {
		private val inventories = mutableMapOf<UUID, InteractableGUI>()

		fun getInventory(uuid: UUID): InteractableGUI? = inventories[uuid]
		fun setInventory(uuid: UUID, gui: InteractableGUI) { inventories[uuid] = gui }

		@EventHandler
		fun onInventoryClick(event: InventoryClickEvent) {
			val inventory = event.clickedInventory ?: return

			val holder = inventory.holder

			if (holder is InteractableGUI) {
				holder.handleClick(event)
			}

			if (holder is Player) {
				val topHolder = holder.openInventory.topInventory.holder

				if (topHolder is InteractableGUI) {
					topHolder.handlePlayerClick(event)
				}

				return
			}
		}

		@EventHandler
		fun onInventoryDrag(event: InventoryDragEvent) {
			val inventory = event.inventory

			val holder = inventory.holder

			if (holder is InteractableGUI) {
				holder.handleDrag(event)
			}
		}

		@EventHandler
		fun onInventoryClose(event: InventoryCloseEvent) {
			val inventory = event.inventory
			val holder = inventory.holder

			if (holder is InteractableGUI) {
				holder.handleClose(event)
			}
		}
	}
}
