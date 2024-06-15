package net.horizonsend.ion.server.features.gui.custom.slot

import net.horizonsend.ion.server.features.gui.custom.CustomGUI
import org.bukkit.inventory.ItemStack

/**
 * Represents a GUI slot, which can hold items
 *
 * Designed for
 **/
abstract class GUISlot(val slot: Int, val gui: CustomGUI) {
	/**
	 * Check whether the player can add items from this slot
	 **/
	abstract fun playerCanAdd(itemStack: ItemStack): Boolean

	/**
	 * Check whether the player can remove the item from this slot
	 **/
	abstract fun playerCanRemove(): Boolean

	/**
	 * Get the item in this slot
	 **/
	fun getGuiItem() = gui.getItem(slot)

	fun getRawItem(): ItemStack? = gui.getAllInventories().first().getUnsafeItem(slot)
}
