package net.horizonsend.ion.server.features.gui.custom.slot

import net.horizonsend.ion.server.features.gui.custom.ChangeListener
import net.horizonsend.ion.server.features.gui.custom.CustomGUI
import xyz.xenondevs.invui.item.Item

/**
 * A slot that
 **/
abstract class CalculatedSlot(slot: Int, gui: CustomGUI) : GUISlot(slot, gui), ChangeListener {
	/**
	 * Calculate the item in this slot
	 **/
	abstract fun calculateItem(): Item

	override fun handleChange() {
		calculateItem()
	}
}
