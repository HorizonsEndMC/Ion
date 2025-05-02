package net.horizonsend.ion.server.features.client.display.modular.display

// Text display scale that matches the line height of sign text
const val MATCH_SIGN_FONT_SIZE = 0.416f

// In blocks, the height of a single line of text on a sign
const val SIGN_LINE_HEIGHT = 0.0728
// In blocks, the amount of space between lines of text on a sign
const val SIGN_LINE_SPACING = 0.03134479271

// In blocks, the amount of padding between the bottom of the block and the bottom of the sign model
const val SIGN_BOTTOM_PADDING = 0.26895854398
// In blocks, the amount of padding between the top of the block and the top of the sign model
const val SIGN_TOP_PADDING = 0.22851365015
// In blocks, height of the sign model
const val SIGN_HEIGHT = 0.50252780586

// In blocks, amount of empty space between the top of the sign model and the top of the text of the first line
const val SIGN_TEXT_PADDING_TOP = 0.04246713852
// In blocks, amount of empty space between the bottom of the sign model and the bottom of the text of the last line
const val SIGN_TEXT_PADDING_BOTTOM = 0.07280080889

// Gets a Y offset that corresponds to the line number provided
fun getLinePos(lineNum: Int): Double {
	val alignmentOffset = SIGN_LINE_HEIGHT / 7.0
	val originLineOffset = SIGN_TOP_PADDING + SIGN_TEXT_PADDING_TOP
	val lineOffset =  (SIGN_LINE_SPACING + SIGN_LINE_HEIGHT) * lineNum

	return ((1.0 - originLineOffset) - lineOffset) + alignmentOffset
}

val POWER_TEXT_LINE = getLinePos(3)
val STATUS_TEXT_LINE = getLinePos(4)
