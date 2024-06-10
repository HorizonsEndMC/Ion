package net.horizonsend.ion.server.features.gui.custom

import net.horizonsend.ion.server.features.gui.custom.slot.GUISlot
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.AbstractGui
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.SlotElement
import xyz.xenondevs.invui.inventory.Inventory
import xyz.xenondevs.invui.inventory.event.PlayerUpdateReason
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.item.impl.SimpleItem

open class CustomGUI(val location: Location, width: Int, height: Int) : AbstractGui(width, height) {
	private val occupiedItems: MutableMap<Int, ItemStack> = mutableMapOf()
	private val slots: MutableMap<Int, GUISlot> = mutableMapOf()

	private val changeListeners: MutableList<ChangeListener> = mutableListOf()

	fun addSlot(index: Int, slot: GUISlot) {
		slots[index] = slot

		if (slot is ChangeListener) changeListeners.add(slot)
	}

	private fun setItem(slot: Int, item: ItemStack) {
		println("Adding $item to slot $slot")
		occupiedItems[slot] = item.clone()
	}

	private fun removeItem(slot: Int) {
		val item = occupiedItems.remove(slot)
		println("Removed $item from slot $slot")
	}

	private fun handleAddItem(index: Int, newItem: ItemStack, event: Cancellable): Boolean {
		println("Handle add")
		val slot = slots[index] ?: run {
			event.isCancelled = true
			return false
		}

		if (!slot.playerCanAdd(newItem)) {
			event.isCancelled = true
			return false
		}

		setItem(index, newItem)
		println("Player could add")
		return true
	}

	private fun handleRemoveItem(index: Int, event: Cancellable): Boolean {
		println("Handle remove")
		val slot = slots[index] ?: run {
			event.isCancelled = true
			return false
		}

		if (!slot.playerCanRemove()) {
			event.isCancelled = true
			return false
		}

		removeItem(index)
		println("Player could remove")
		return true
	}

	private fun handleSwapItem(index: Int, oldItem: ItemStack, newItem: ItemStack, event: Cancellable): Boolean {
		println("Handle swap")
		val slot = slots[index] ?: run {
			event.isCancelled = true
			return false
		}
		return false
	}

	override fun handleClick(slotNumber: Int, player: Player, clickType: ClickType?, event: InventoryClickEvent) {
		println("Got click")
		println("click type: $clickType")
		println("Cursor: ${event.cursor}")
		println("Current item: ${event.currentItem}")

		val index = event.slot
		val cursorItem = event.cursor
		val currentItem = event.currentItem

		when {
			event.click == ClickType.NUMBER_KEY -> handleNumberKey(slotNumber, player, clickType, event)
			currentItem == null && cursorItem.type != Material.AIR -> handleAddItem(index, cursorItem, event)
			currentItem != null && cursorItem.type == Material.AIR -> handleRemoveItem(index, event)
			currentItem != null && cursorItem.type != Material.AIR -> handleSwapItem(index, currentItem, cursorItem, event)
		}

//		super.handleClick(slotNumber, player, clickType, event)
	}

	private fun handleNumberKey(slotNumber: Int, player: Player, clickType: ClickType?, event: InventoryClickEvent) {
		val playerItem = player.inventory.getItem(event.hotbarButton) ?: ItemStack(Material.AIR)
		val currentItem = event.currentItem

		println("Got number key")
		println("Key: ${event.hotbarButton}")
		println("Player hotbar item: $playerItem")
		println("Slot1: $slotNumber")
		println("Slot2: ${event.slot}")
		println("SlotType: ${event.slotType}")

		when {
			currentItem == null && playerItem.type != Material.AIR -> handleAddItem(slotNumber, playerItem, event)
			currentItem != null && playerItem.type == Material.AIR -> handleRemoveItem(slotNumber, event)
			currentItem != null && playerItem.type != Material.AIR -> handleSwapItem(slotNumber, currentItem, playerItem, event)
		}
	}

	override fun handleItemDrag(updateReason: UpdateReason, slot: Int, oldStack: ItemStack?, newStack: ItemStack?): Boolean {
		updateReason as PlayerUpdateReason
		println("got item drag")
		println("Update reason: $updateReason")
		println("slot: $slot")
		println("oldStack: $oldStack")
		println("newStack: $newStack")
		val event = updateReason.event as InventoryDragEvent

		val cursorItem = event.cursor ?: run {
			println("No cursor item")
			event.isCancelled = true
			return false
		}

		val currentItem = event.newItems.entries.firstOrNull()?.value

		return when {
			currentItem == null && cursorItem.type != Material.AIR -> handleAddItem(slot, cursorItem, event)
			currentItem != null && cursorItem.type == Material.AIR -> handleRemoveItem(slot, event)
			currentItem != null && cursorItem.type != Material.AIR -> handleSwapItem(slot, currentItem, cursorItem, event)
			else -> {
				println("else")
				false
			}
		}
	}

	override fun handleInvLeftClick(event: InventoryClickEvent?, inventory: Inventory?, slot: Int, player: Player?, clicked: ItemStack?, cursor: ItemStack?) {
		println("Got left click")
		super.handleInvLeftClick(event, inventory, slot, player, clicked, cursor)
	}

	override fun handleItemShift(event: InventoryClickEvent) {
		println("Got shift")
		println("click type: ${event.click}")
		println("Cursor: ${event.cursor}")
		println("Current item: ${event.currentItem}")
		println("slot: ${event.slot}")

		val moveditem = event.currentItem

		when {
			// Try to find the first slot that it can go into
			moveditem != null -> {
				val firstSlot = slots.values.firstOrNull { it.playerCanAdd(moveditem) }?.slot

				if (firstSlot == null) {
					event.isCancelled = true
					return
				}

				// Occupied
				if (getItem(firstSlot) != null) {
					event.isCancelled = true
					return
				}

				// Set it visually
				setItem(firstSlot, SimpleItem(moveditem))
				handleAddItem(firstSlot, moveditem, event)

				// Remove it from the inventory
				event.playerClicker.inventory.setItem(event.slot, ItemStack(Material.AIR))
				event.isCancelled = true
			}
		}

//		super.handleItemShift(event)
	}

	override fun handleInvDrop(ctrl: Boolean, event: InventoryClickEvent?, inventory: Inventory?, slot: Int, player: Player?, clicked: ItemStack?) {
		println("Got inventory drop")
		super.handleInvDrop(ctrl, event, inventory, slot, player, clicked)
	}

	override fun handleInvRightClick(event: InventoryClickEvent?, inventory: Inventory?, slot: Int, player: Player?, clicked: ItemStack?, cursor: ItemStack?) {
		println("Got right click")
		super.handleInvRightClick(event, inventory, slot, player, clicked, cursor)
	}

	override fun handleInvDoubleClick(event: InventoryClickEvent?, player: Player?, cursor: ItemStack?) {
		println("Got double click")
		super.handleInvDoubleClick(event, player, cursor)
	}

	override fun handleInvItemShift(event: InventoryClickEvent?, inventory: Inventory?, slot: Int, player: Player?, clicked: ItemStack?) {
		println("Got inv item shift")
		super.handleInvItemShift(event, inventory, slot, player, clicked)
	}

	override fun handleSlotElementUpdate(child: Gui?, slotIndex: Int) {
		println("Got slot element update")
		super.handleSlotElementUpdate(child, slotIndex)
	}

	override fun handleInvOffHandKey(event: InventoryClickEvent?, inventory: Inventory?, slot: Int, player: Player?, clicked: ItemStack?) {
		println("Got offhand key")
		super.handleInvOffHandKey(event, inventory, slot, player, clicked)
	}

	override fun handleInvNumberKey(event: InventoryClickEvent?, inventory: Inventory?, slot: Int, player: Player?, clicked: ItemStack?) {
		println("Got number key")
		super.handleInvNumberKey(event, inventory, slot, player, clicked)
	}

	override fun handleInvSlotElementClick(element: SlotElement.InventorySlotElement?, event: InventoryClickEvent?) {
		println("Got element slot click")
		super.handleInvSlotElementClick(element, event)
	}

	override fun closeForAllViewers() {
		super.closeForAllViewers()
	}

	val closeHandler: Runnable = Runnable {
		println("Running close handler. Items: $occupiedItems")

		for ((_, item) in occupiedItems) {
			location.world.dropItem(location, item)
		}
	}
}
