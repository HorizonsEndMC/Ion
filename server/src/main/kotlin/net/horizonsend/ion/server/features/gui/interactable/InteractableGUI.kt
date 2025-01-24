package net.horizonsend.ion.server.features.gui.interactable

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftContainer
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
import java.util.function.Consumer

abstract class InteractableGUI(protected val viewer: Player) : InventoryHolder, GuiWrapper {
	protected abstract val internalInventory: Inventory
	abstract val inventorySize: Int

	override fun getInventory(): Inventory {
		return internalInventory
	}

	protected val buttons = mutableMapOf<Int, Consumer<InventoryClickEvent>>()
	protected val noDropSlots: MutableSet<Int> = mutableSetOf()
	protected val lockedSlots: MutableSet<Int> = mutableSetOf()

	override fun open() {
		// Will return CRAFTING if none is open
		if (viewer.openInventory.type != InventoryType.CRAFTING && viewer.openInventory.type != InventoryType.CREATIVE) return

		val view = viewer.openInventory(inventory) ?: return

		setup(view)
	}

	fun addGuiButton(slot: Int, item: ItemStack, function: Consumer<InventoryClickEvent>) {
		internalInventory.setItem(slot, item)
		buttons[slot] = function
		noDropSlots.add(slot)
		lockedSlots.add(slot)
	}

	abstract fun setup(view: InventoryView)

	abstract fun handleClose(event: InventoryCloseEvent)

	abstract fun canAdd(itemStack: ItemStack, slot: Int, player: Player): Boolean
	abstract fun canRemove(slot: Int, player: Player): Boolean
	abstract fun itemChanged(changedSlot: Int, changedItem: ItemStack)

	open fun handleAddItem(slot: Int, item: ItemStack, event: InventoryClickEvent) {
		buttons[slot]?.accept(event)

		if (lockedSlots.contains(slot) || !canAdd(item, slot, event.whoClicked as Player)) {
			event.isCancelled = true
			return
		}

		itemChanged(slot, item)
	}

	open fun handleAddItem(slot: Int, item: ItemStack, event: InventoryInteractEvent) {
		if (lockedSlots.contains(slot) || !canAdd(item, slot, event.whoClicked as Player)) {
			event.isCancelled = true
			return
		}

		itemChanged(slot, item)
	}

	open fun handleRemoveItem(slot: Int, event: InventoryClickEvent) {
		buttons[slot]?.accept(event)
		if (lockedSlots.contains(slot) || !canRemove(slot, event.whoClicked as Player)) {
			event.isCancelled = true
			return
		}

		itemChanged(slot, internalInventory.contents[slot]!!)
	}

	open fun handleSwapItem(slot: Int, currentItem: ItemStack, new: ItemStack, event: InventoryClickEvent) {
		buttons[slot]?.accept(event)
		if (lockedSlots.contains(slot) || !canAdd(new, slot, event.playerClicker)) {
			event.isCancelled = true
			return
		}

		val modified = internalInventory.contents.toMutableList()
		modified[slot] = new

		itemChanged(slot, new)
	}

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
			event.rawSlots.size == 1 -> {
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
		fun setInventory(uuid: UUID, gui: InteractableGUI?) {
			if (gui == null) {
				inventories.remove(uuid)
				return
			}

			inventories[uuid] = gui
		}

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

		/**
		 * Updates the title of this inventory
		 **/
		fun InventoryView.setTitle(title: Component) {
			val entityPlayer = (player as Player).minecraft
			val containerId = entityPlayer.containerMenu.containerId
			val windowType = CraftContainer.getNotchInventoryType(topInventory)
			entityPlayer.connection.send(ClientboundOpenScreenPacket(containerId, windowType, PaperAdventure.asVanilla(title)))
			(player as Player).updateInventory()
		}
	}

	/**
	 * Drops all items in the inventory not added to the noDrop list
	 **/
	protected fun dropItems(location: Location) {
		for ((slot, content) in inventory.contents.withIndex()) {
			if (noDropSlots.contains(slot)) continue
			viewer.world.dropItemNaturally(location, content?.clone() ?: continue)
			content.amount = 0 // Ensure no dupes
		}
	}

	/**
	 * Gets all items in the inventory not marked as locked
	 **/
	protected fun getUnlockedItems(): List<ItemStack> {
		return internalInventory.contents.withIndex()
			.filterNot { lockedSlots.contains(it.index) }
			.mapNotNull { it.value }
	}
}
