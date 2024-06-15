package net.horizonsend.ion.server.features.gui.custom

import net.horizonsend.ion.server.IonServer
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
import xyz.xenondevs.invui.inventory.event.PlayerUpdateReason
import xyz.xenondevs.invui.inventory.event.UpdateReason
import xyz.xenondevs.invui.item.impl.SimpleItem

open class CustomGUI(val location: Location, width: Int, height: Int) : AbstractGui(width, height) {
	protected val occupiedItems: MutableMap<Int, ItemStack> = mutableMapOf()
	protected val slots: MutableMap<Int, GUISlot> = mutableMapOf()

	private val changeListeners: MutableList<ChangeListener> = mutableListOf()

	fun addSlot(index: Int, slot: GUISlot) {
		slots[index] = slot

		if (slot is ChangeListener) changeListeners.add(slot)
	}

	fun notifyChange() {
		for (changeListener in changeListeners) {
			runCatching { changeListener.handleChange() }.onFailure {
				IonServer.slF4JLogger.error("Error processing change in GUI!")
				it.printStackTrace()
			}
		}
	}

	private fun setItem(slot: Int, item: ItemStack) {
		println("Adding $item to slot $slot")
		occupiedItems[slot] = item.clone()
		setItem(slot, SimpleItem(item))

		notifyChange()
	}

	private fun removeItem(slot: Int): ItemStack? {
		val item = occupiedItems.remove(slot)
//		remove(slot)
		notifyChange()
		println("Removed $item from slot $slot")

		return item
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

	private fun handleRemoveItem(index: Int, event: Cancellable, updateCursor: (ItemStack) -> Unit = {}): Boolean {
		println("Handle remove")
		val slot = slots[index] ?: run {
			event.isCancelled = true
			return false
		}

		if (!slot.playerCanRemove()) {
			event.isCancelled = true
			return false
		}

		// Prevents possible duplication glitches
		val removedItemStack = removeItem(index)
		if (removedItemStack == null) {
			println("Remove failed, item not in map")
			event.isCancelled = true
			return false
		}

		println("Player could remove")
		updateCursor.invoke(removedItemStack)
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
			currentItem != null && cursorItem.type == Material.AIR -> handleRemoveItem(slot, event) { event.cursor = it }
			currentItem != null && cursorItem.type != Material.AIR -> handleSwapItem(slot, currentItem, cursorItem, event)
			else -> {
				println("else")
				false
			}
		}
	}

	override fun handleItemShift(event: InventoryClickEvent) {
		println("Got shift")
		println("click type: ${event.click}")
		println("Cursor: ${event.cursor}")
		println("Current item: ${event.currentItem}")
		println("slot: ${event.slot}")

		val movedItem = event.currentItem

		when {
			// Try to find the first slot that it can go into
			movedItem != null -> {
				val firstSlot = slots.values.filter { it.playerCanAdd(movedItem) }.minByOrNull { it.slot }?.slot

				if (firstSlot == null) {
					event.isCancelled = true
					return
				}
				println("Current item: ${event.inventory.getItem(firstSlot)}")

				// Occupied
				if (event.inventory.getItem(firstSlot) != null) {
					event.isCancelled = true
					return
				}

				// Set it visually
				setItem(firstSlot, SimpleItem(movedItem))
				handleAddItem(firstSlot, movedItem, event)

				// Remove it from the inventory
				event.playerClicker.inventory.setItem(event.slot, ItemStack(Material.AIR))
				event.isCancelled = true
			}
		}
	}

	val closeHandler: Runnable = Runnable {
		println("Running close handler. Items: $occupiedItems")

		for ((_, item) in occupiedItems) {
			location.world.dropItem(location.add(0.0, 0.5, 0.0), item)
		}
	}
}
