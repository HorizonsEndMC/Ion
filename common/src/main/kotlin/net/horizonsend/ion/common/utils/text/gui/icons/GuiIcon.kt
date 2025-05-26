package net.horizonsend.ion.common.utils.text.gui.icons

import net.horizonsend.ion.common.utils.text.GUI_HEADER_MARGIN
import net.horizonsend.ion.common.utils.text.SLOT_OVERLAY_WIDTH
import net.horizonsend.ion.common.utils.text.leftShift
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.shift
import net.horizonsend.ion.common.utils.text.shiftToLine
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor

abstract class GuiIcon(val type: GuiIconType) {
	abstract fun buildcomponent(): Component

	open fun getComponent(line: Int): Component {
		// The width of a slot is 18. In order to ready the next slot, we need to work from the starting point of the end of this slot
		// To shift back to it, we need to shift over how much this slot sticks into the next, which is (the width - a regular slot - how much it was initially shifted left)
		val finalRightShift = (SLOT_OVERLAY_WIDTH - type.width) - type.shift

		return ofChildren(shift(type.shift - 1), buildcomponent().shiftToLine(line, GUI_HEADER_MARGIN), shift(finalRightShift))
	}

	open class Simple(type: GuiIconType) : GuiIcon(type) {
		override fun buildcomponent(): Component = text(type.displayChar, WHITE)
	}

	open class Icon(private val color: TextColor, private val bordered: Boolean, type: GuiIconType) : GuiIcon(type) {
		override fun buildcomponent(): Component = text(type.displayChar, color)

		override fun getComponent(line: Int): Component {
			return if (bordered) {
				ofChildren(super.getComponent(line), leftShift(SLOT_OVERLAY_WIDTH - 1), iconBorder().getComponent(line))
			}
			else return super.getComponent(line)
		}
	}

	data object Empty : GuiIcon(GuiIconType.EMPTY) {
		override fun getComponent(line: Int): Component = shift(SLOT_OVERLAY_WIDTH)
		override fun buildcomponent(): Component = shift(SLOT_OVERLAY_WIDTH)
	}

	companion object {
		val EMPTY = Empty

		fun textInputBoxLeft() = Simple(GuiIconType.LEFT_TEXT_BOX)
		fun textInputBoxCenter() = Simple(GuiIconType.CENTER_TEXT_BOX)
		fun textInputBoxRight() = Simple(GuiIconType.RIGHT_TEXT_BOX)

		private fun iconBorder() = Simple(GuiIconType.ICON_BORDER)

		fun pencilIcon(color: TextColor, bordered: Boolean) = Icon(color, bordered, GuiIconType.PENCIL_ICON)
		fun trashCanIcon(color: TextColor, bordered: Boolean) = Icon(color, bordered, GuiIconType.TRASH_CAN_ICON)
		fun checkmarkIcon(color: TextColor, bordered: Boolean) = Icon(color, bordered, GuiIconType.CHECKMARK_ICON)
		fun crossIcon(color: TextColor, bordered: Boolean) = Icon(color, bordered, GuiIconType.CROSS_ICON)
		fun emptyIcon(color: TextColor, bordered: Boolean) = Icon(color, bordered, GuiIconType.EMPTY_ICON)
	}
}
