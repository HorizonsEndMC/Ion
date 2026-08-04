package net.horizonsend.ion.common.utils.text.gui

import net.horizonsend.ion.common.utils.text.GUI_HEADER_MARGIN
import net.horizonsend.ion.common.utils.text.GUI_MARGIN
import net.horizonsend.ion.common.utils.text.SIMPLE_GUI_BORDER_CHARACTER
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.shift
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor

class GuiBorder(private val displayChar: Char, val width: Int, val color: TextColor, val headerIcon: HeaderIcon?, val leftText: Component?, val rightText: Component?) {
	fun build(): Component {
		val difference = width - GUI_TOTAL_WIDTH
		val initialShift = difference / 2
		val secondShift = -(GUI_HEADER_MARGIN + GUI_TOTAL_WIDTH - (difference + 1))

		return ofChildren(ofChildren(shift(-(initialShift + GUI_MARGIN)), text(displayChar, color).font(SPECIAL_FONT_KEY), shift(secondShift)))
	}

	companion object {
		private const val GUI_TOTAL_WIDTH = 176

		fun regular(
			color: TextColor,
			headerIcon: HeaderIcon? = null,
			leftText: Component? = null,
			rightText: Component? = null,
		): GuiBorder = GuiBorder(SIMPLE_GUI_BORDER_CHARACTER, 182, color, headerIcon, leftText, rightText)
	}

	class HeaderIcon(private val displayChar: Char, val width: Int, val color: TextColor) {
		fun build(): Component {
			val firstShift = -((GUI_HEADER_MARGIN + 1) * 2) + ((GUI_TOTAL_WIDTH - width) / 2)
			val secondShift = -(((GUI_TOTAL_WIDTH + width) / 2) - GUI_MARGIN + 1)

			return ofChildren(shift(firstShift), text(displayChar, color).font(SPECIAL_FONT_KEY), shift(secondShift))
		}
	}
}
