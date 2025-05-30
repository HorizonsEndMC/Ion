package net.horizonsend.ion.common.utils.text.gui.icons

import net.horizonsend.ion.common.utils.text.CHECKMARK_CHARACTER
import net.horizonsend.ion.common.utils.text.CROSS_CHARACTER
import net.horizonsend.ion.common.utils.text.DEPOSIT_ICON
import net.horizonsend.ion.common.utils.text.EMPTY_ICON_CHARACTER
import net.horizonsend.ion.common.utils.text.ICON_BORDER_CHARACTER
import net.horizonsend.ion.common.utils.text.PENCIL_CHARACTER
import net.horizonsend.ion.common.utils.text.RED_SLOT_ICON
import net.horizonsend.ion.common.utils.text.SLOT_OVERLAY_WIDTH
import net.horizonsend.ion.common.utils.text.TEXT_INPUT_CENTER_CHARACTER
import net.horizonsend.ion.common.utils.text.TEXT_INPUT_LEFT_CHARACTER
import net.horizonsend.ion.common.utils.text.TEXT_INPUT_RIGHT_CHARACTER
import net.horizonsend.ion.common.utils.text.TRASHCAN_CHARACTER
import net.horizonsend.ion.common.utils.text.WITHDRAW_ICON

enum class GuiIconType(val displayChar: Char, val width: Int = SLOT_OVERLAY_WIDTH, val shift: Int = 0) {
	EMPTY('\uFFFF'),

	SLOT(RED_SLOT_ICON, width = 16, shift = 1),

	LEFT_TEXT_BOX(TEXT_INPUT_LEFT_CHARACTER),
	CENTER_TEXT_BOX(TEXT_INPUT_CENTER_CHARACTER),
	RIGHT_TEXT_BOX(TEXT_INPUT_RIGHT_CHARACTER),

	PENCIL_ICON(PENCIL_CHARACTER, width = 46, shift = -14),
	TRASH_CAN_ICON(TRASHCAN_CHARACTER, width = 46, shift = -14),
	CHECKMARK_ICON(CHECKMARK_CHARACTER, width = 46, shift = -14),
	CROSS_ICON(CROSS_CHARACTER, width = 46, shift = -14),
	EMPTY_ICON(EMPTY_ICON_CHARACTER, width = 46, shift = -14),
	DEPOSIT(DEPOSIT_ICON, width = 46, shift = -14),
	WITHDRAW(WITHDRAW_ICON, width = 46, shift = -14),

	ICON_BORDER(ICON_BORDER_CHARACTER, width = 48, shift = -16),

	;

	companion object {
		private val displayChar = entries.associateBy { it.displayChar }
		fun getByDisplayChar(char: Char) = displayChar[char]
	}
}
