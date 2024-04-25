package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/** Skip building the serializer */
val miniMessage = MiniMessage.miniMessage()

/** Serializes the component to minimessage format */
fun miniMessage(component: Component): String = miniMessage.serialize(component)

/** Converts the provided MiniMessage string to a component using the MiniMessage serializer. */
fun String.miniMessage() = miniMessage.deserialize(this)

/** Skip building the serializer */
val plainText = PlainTextComponentSerializer.plainText()

/** Converts the provided Component to a string using the PlainText serializer. */
fun ComponentLike.plainText(): String = plainText.serialize(this.asComponent())

operator fun Component.plus(other: ComponentLike): Component = this.append(other)

/** Shorthand for Component#textOfChildren */
fun ofChildren(vararg children: ComponentLike) = Component.textOfChildren(*children)

/** Analogue of Any#toString */
@JvmOverloads
fun Any.toComponent(vararg decorations: TextDecoration, color: TextColor = WHITE): Component = text(toString(), color, *decorations)
fun Any.toComponent(color: TextColor = WHITE): Component = text(toString(), color)

/**
 * Formats the number into credit format, so it is rounded to the nearest hundredth,
 * commas are placed every 3 digits to the left of the decimal point,
 * and "C" is placed at the beginning of the string.
 */
fun Number.toCreditComponent(): Component = text("C${toDouble().roundToHundredth().toText()}", NamedTextColor.GOLD)

/** Joins the collection of components using the provided separator between each entry */
fun Iterable<ComponentLike>.join(separator: Component? = text(", ")): Component {
	val iterator = this.iterator()

	val builder = text()

	while (iterator.hasNext()) {
		builder.append(iterator.next())

		if (iterator.hasNext() && separator != null) builder.append(separator)
	}

	return builder.build()
}

/** Returns an empty component if the provided component was null */
fun Component?.orEmpty(): Component = this ?: empty()

fun formatLink(showText: String, link: String): Component {
	return text(showText, BLUE, TextDecoration.UNDERLINED)
		.clickEvent(ClickEvent.openUrl(link))
		.hoverEvent(text(link))
}

val ITALIC = TextDecoration.ITALIC
val BOLD = TextDecoration.BOLD
val UNDERLINED = TextDecoration.UNDERLINED
val STRIKETHROUGH = TextDecoration.STRIKETHROUGH

//<editor-fold desc="Custom GUI helper functions">/

val SPECIAL_FONT_KEY = Key.key("horizonsend:special")
private fun yFontKey(y: Int) = Key.key("horizonsend:y$y")

const val TEXT_HEIGHT = 9
const val DEFAULT_GUI_WIDTH = 169
const val GUI_MARGIN = 8
const val GUI_HEADER_MARGIN = 3

const val SHIFT_LEFT_MIN = 1
const val SHIFT_LEFT_MAX = 169
const val SHIFT_RIGHT_MIN = 1
const val SHIFT_RIGHT_MAX = 169
const val SHIFT_DOWN_MIN = 1
const val SHIFT_DOWN_MAX = 110

// Custom characters begin

const val SHIFT_LEFT_BEGIN = 0xE000
const val SHIFT_LEFT_END = 0xE0A8
const val SHIFT_RIGHT_BEGIN = 0xE100
const val SHIFT_RIGHT_END = 0xE1A8

const val SHIFT_LEFT_BEGIN_MIN_1 = 0xDFFF
const val SHIFT_RIGHT_BEGIN_MIN_1 = 0xE0FF

const val DEFAULT_BACKGROUND_CHAR = '\uF8FF'
const val CHETHERITE_CHARACTER = '\uF8FE'

// Custom characters end

val Component.minecraftLength: Int
	get() = this.plainText().minecraftLength

/**
 * Gets the width (in pixels) of a string rendered in the default Minecraft font.
 */
val String.minecraftLength: Int
	get() = this.sumOf {
		@Suppress("Useless_Cast")
		when (it.code) {
			// for some reason, SHIFT_LEFT_BEGIN and the MIN_1 value does not work here
			in SHIFT_LEFT_BEGIN..SHIFT_LEFT_END -> 0xDFFF - it.code
			in SHIFT_RIGHT_BEGIN..SHIFT_RIGHT_END -> -0xE0FF + it.code
			else -> when (it) {
				'\n' -> 0
				'i', '!', ',', '.', '\'', ':', ';', '|' -> 2
				'l', '`' -> 3
				'I', 't', ' ', '\"', '(', ')', '*', '[', ']', '{', '}' -> 4
				'k', 'f', '<', '>' -> 5
				'@', '~', '«', '»' -> 7
				CHETHERITE_CHARACTER -> 10
				else -> 6
			} as Int
		}
	}

/**
 * Create a new Component, shifting text left or right
 * @param shift number of pixels to shift left between -169 and -1, or number of pixels to shift right between 1 and 169
 */
fun shift(shift: Int): Component {
	return when (shift) {
		in SHIFT_RIGHT_MIN..SHIFT_RIGHT_MAX -> rightShift(shift)
		in -SHIFT_LEFT_MAX..-SHIFT_LEFT_MIN -> leftShift(-shift)
		else -> empty()
	}
}

/**
 * Create a new Component starting with a left shift
 * @param shift number of pixels to shift between 1 and 169
 */
fun leftShift(shift: Int): Component = if (shift in SHIFT_LEFT_MIN..SHIFT_LEFT_MAX) {
	text((SHIFT_LEFT_BEGIN_MIN_1 + shift).toChar()).font(SPECIAL_FONT_KEY)
} else empty()

/**
 * Create a new Component starting with a right shift
 * @param shift number of pixels to shift between 1 and 169
 */
fun rightShift(shift: Int): Component = if (shift in SHIFT_RIGHT_MIN..SHIFT_RIGHT_MAX) {
	text((SHIFT_RIGHT_BEGIN_MIN_1 + shift).toChar()).font(SPECIAL_FONT_KEY)
} else empty()

/**
 * Add a left shift that returns the text to the left (beginning) of the Component
 */
fun Component.shiftToLeftOfComponent(): Component {
	return this.append(leftShift(this.minecraftLength))
}

/**
 * Add a downward shift to the entire Component
 * @param shift number of pixels to shift between 1 and 110
 */
fun Component.shiftDown(shift: Int): Component = if (shift in SHIFT_DOWN_MIN..SHIFT_DOWN_MAX) {
	this.font(yFontKey(shift))
} else this

/**
 * Add a downward shift to the entire Component equivalent to the next line, plus some offset
 * @param line number of lines to shift down
 * @param shift number of additional pixels to shift down
 */
fun Component.shiftToLine(line: Int, shift: Int = 0): Component = this.shiftDown((line + 1) * TEXT_HEIGHT + shift)

/**
 * Splits the text of the current component so that the text fits within a certain width
 * Implementation based on https://stackoverflow.com/questions/17586/best-word-wrap-algorithm
 * @param width the width in pixels to limit the text to
 */
fun Component.wrap(width: Int) {
	// regex: positive lookbehind, matching any character that is newline, tab, space, or hyphen
	val regex = Regex("(?<=[\n\t -])")
	// when combined with String.split(), it includes the delimiter with the last word instead of removing it
	val words = this.plainText().split(regex)

	var currentLength = 0
	val stringBuilder = StringBuilder()

	for (word in words) {
		val length = word.minecraftLength
		val exceedsWidth = length > width

		// add new line
		if (currentLength + length > width) {
			// check if there is only whitespace
			if (currentLength > 0) {
				stringBuilder.append('\n')
				currentLength = 0
			}

			// split word up if its own length exceeds width
			if (exceedsWidth) {
				var longWord = word
				while (longWord.minecraftLength > width) {
					stringBuilder.append(word.substring(0, width - 1))
					longWord = word.substring(width - 1)

					stringBuilder.append('\n')
				}
				stringBuilder.append(longWord.trimStart())
			}
		}
		if (!exceedsWidth) stringBuilder.append(word.trimStart())
		currentLength += length
	}

	// regex: match everything
	val replacer = TextReplacementConfig.builder().match("[\\s\\S]+").replacement(stringBuilder.toString()).build()
	this.replaceText(replacer)
}

//</editor-fold>