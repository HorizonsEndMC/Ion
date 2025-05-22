package net.horizonsend.ion.common.utils.text.icons

import net.horizonsend.ion.common.utils.text.CHECKMARK_CHARACTER
import net.horizonsend.ion.common.utils.text.EMPTY_ICON_CHARACTER
import net.horizonsend.ion.common.utils.text.ICON_BORDER_CHARACTER
import net.horizonsend.ion.common.utils.text.PENCIL_CHARACTER
import net.horizonsend.ion.common.utils.text.SLOT_OVERLAY_WIDTH
import net.horizonsend.ion.common.utils.text.TEXT_INPUT_CENTER_CHARACTER
import net.horizonsend.ion.common.utils.text.TEXT_INPUT_LEFT_CHARACTER
import net.horizonsend.ion.common.utils.text.TEXT_INPUT_RIGHT_CHARACTER
import net.horizonsend.ion.common.utils.text.TRASHCAN_CHARACTER

enum class GuiIconType(val displayChar: Char, val width: Int = SLOT_OVERLAY_WIDTH, val shift: Int = 0) {
	EMPTY('\uFFFF'),

	LEFT_TEXT_BOX(TEXT_INPUT_LEFT_CHARACTER),
	CENTER_TEXT_BOX(TEXT_INPUT_CENTER_CHARACTER),
	RIGHT_TEXT_BOX(TEXT_INPUT_RIGHT_CHARACTER),

	PENCIL_ICON(PENCIL_CHARACTER, width = 30, shift = -6),
	TRASH_CAN_ICON(TRASHCAN_CHARACTER, width = 30, shift = -6),
	CHECKMARK_ICON(CHECKMARK_CHARACTER, width = 30, shift = -6),
	EMPTY_ICON(EMPTY_ICON_CHARACTER, width = 30, shift = -6),

	ICON_BORDER(ICON_BORDER_CHARACTER, width = 32, shift = -8),

	;

	companion object {
		private val displayChar = entries.associateBy { it.displayChar }
		fun getByDisplayChar(char: Char) = displayChar[char]
	}
}
