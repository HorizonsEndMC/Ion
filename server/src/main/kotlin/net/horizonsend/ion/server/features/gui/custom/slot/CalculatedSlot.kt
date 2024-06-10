package net.horizonsend.ion.server.features.gui.custom.slot

import net.horizonsend.ion.server.features.gui.custom.ChangeListener
import net.horizonsend.ion.server.features.gui.custom.CustomGUI
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.Item

/**
 * A slot that
 **/
abstract class CalculatedSlot(slot: Int, gui: CustomGUI) : GUISlot(slot, gui), ChangeListener {
	final override fun playerCanAdd(itemStack: ItemStack): Boolean {
		return false
	}

	/**
	 * Calculate the item in this slot
	 **/
	abstract fun calculateItem(): Item

	override fun handleChange() {
		calculateItem()
	}
}
