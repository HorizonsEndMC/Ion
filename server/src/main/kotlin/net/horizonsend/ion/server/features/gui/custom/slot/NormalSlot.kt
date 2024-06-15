package net.horizonsend.ion.server.features.gui.custom.slot

import net.horizonsend.ion.server.features.gui.custom.ChangeListener
import net.horizonsend.ion.server.features.gui.custom.CustomGUI
import org.bukkit.inventory.ItemStack

class NormalSlot(
	index: Int,
	gui: CustomGUI,
	val canAdd: (ItemStack) -> Boolean,
	val canRemove: NormalSlot.() -> Boolean
) : GUISlot(index, gui) {
	override fun playerCanAdd(itemStack: ItemStack): Boolean {
		return canAdd(itemStack)
	}

	override fun playerCanRemove(): Boolean {
		return canRemove(this)
	}

	fun withListener(changeHandler: () -> Unit): GUISlot = object : GUISlot(slot, gui), ChangeListener {
		override fun playerCanAdd(itemStack: ItemStack): Boolean {
			return canAdd(itemStack)
		}

		override fun playerCanRemove(): Boolean {
			return canRemove(this@NormalSlot)
		}

		override fun handleChange() {
			changeHandler()
		}
	}
}
