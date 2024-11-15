package net.horizonsend.ion.server.features.custom.items.type.tool.mods

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.ModManager
import net.horizonsend.ion.server.features.gui.interactable.InteractableGUI
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.listener.SLEventListener
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

class ToolModMenu(
	viewer: Player,
	private val itemStack: ItemStack,
	private val customItem: CustomItem,
	private val modManager: ModManager
) : InteractableGUI(viewer) {
	override val inventorySize = (ceil((modManager.getMods(itemStack).size + 1).toDouble() / 9.0) * 9).toInt()
	override val internalInventory: Inventory = IonServer.server.createInventory(this, inventorySize)

	override fun getInventory(): Inventory = internalInventory

	override fun setup(view: InventoryView) {
		view.setTitle(text("Tool Modifications"))

		populateMods()
	}

	private fun populateMods() {
		val mods = modManager.getMods(itemStack)

		var index = 0
		for (mod in mods) {
			val modCustomItem = mod.modItem.get() ?: continue
			internalInventory.setItem(index, modCustomItem.constructItemStack())

			index++
		}
	}

	private fun rebuildFromContents() {
		rebuildFromContents(internalInventory.contents.toList())
	}

	private fun rebuildFromContents(contents: Collection<ItemStack?>) {
		val existingMods = modManager.getMods(itemStack).toMutableList()

		val nonItemMods = modManager
			.getMods(itemStack)
			.filter { it.modItem.get() == null }

		val mods = contents
			.mapNotNull { it?.customItem }
			.filterIsInstance<ModificationItem>()
			.mapTo(mutableSetOf()) { it.modification }
			.plus(nonItemMods)
			.toTypedArray()

		val newMods = mods.toMutableList()

		// Remove all the mods that were present
		newMods.removeAll(existingMods)

		// Remove all the mods that still are present
		existingMods.removeAll(mods.toSet())

		modManager.setMods(itemStack, customItem, mods)

		// Handle the removal / addition of mods
		existingMods.forEach { it.onRemove(itemStack) }
		newMods.forEach { it.onAdd(itemStack) }
	}

	/**
	 * Fired when the inventory is closed
	 **/
	override fun handleClose(event: InventoryCloseEvent) {
		rebuildFromContents()
	}

	override fun handleAddItem(slot: Int, item: ItemStack, event: InventoryInteractEvent) {
		if (!canAdd(item, event.whoClicked as Player)) {
			event.isCancelled = true
			return
		}

		rebuildFromContents(listOf(*internalInventory.contents, item))
	}

	override fun handleRemoveItem(slot: Int, event: InventoryClickEvent) {
		// Rebuild with the item removed
		rebuildFromContents(internalInventory.contents.subtract(setOf(internalInventory.contents[slot])))
	}

	override fun handleSwapItem(slot: Int, currentItem: ItemStack, new: ItemStack, event: InventoryClickEvent) {
		if (!canAdd(new, event.playerClicker)) {
			event.isCancelled = true
			return
		}

		val modified = internalInventory.contents.toMutableList()
		modified[slot] = new

		rebuildFromContents(modified)
	}

	private fun canAdd(itemStack: ItemStack, player: Player): Boolean {
		val customItem = itemStack.customItem
		if (customItem !is ModificationItem) {
			return false
		}

		val mod: ItemModification = customItem.modification

		if (!mod.applicationPredicates.any { predicate -> predicate.canApplyTo(this.customItem) }) {
			player.userError("${mod.displayName.plainText()} cannot be used on this tool!")
			return false
		}

		if (this.modManager.getMods(this.itemStack).size >= this.modManager.maxMods) {
			player.userError("Mod limit reached!")
			return false
		}

		return this.modManager.getMods(this.itemStack).none { existingMod ->
			val incompatible = existingMod.incompatibleWithMods.contains(mod::class)

			if (incompatible) {
				player.userError("${mod.displayName.plainText()} is incompatible with ${existingMod.displayName.plainText()}!")
			}

			// Already installed
			if (existingMod == mod) {
				player.userError("${mod.displayName.plainText()} is already installed!")
				return@none true
			}

			incompatible
		}
	}

	companion object : SLEventListener() {
		fun create(viewer: Player, itemStack: ItemStack, customItem: CustomItem, manager: ModManager): ToolModMenu {
			val holder = ToolModMenu(viewer, itemStack, customItem, manager)
			setInventory(viewer.uniqueId, holder)

			return holder

		}

		@EventHandler
		fun onPlayerDropItem(event: PlayerDropItemEvent) {
			val player = event.player
			val holder = player.openInventory.topInventory.holder

			if (ToolModMenu) {
				// Assume they dropped the item

				if (event.itemDrop.itemStack.itemMeta == holder.itemStack.itemMeta) {
					player.closeInventory()
				}
			}
		}
	}
}
