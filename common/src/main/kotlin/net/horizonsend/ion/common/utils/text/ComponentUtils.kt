package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
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
const val RIGHT_EDGE_SHIFT = 161

/**
 * Gets the width (in pixels) of a string rendered in the default Minecraft font.
 */
val String.minecraftLength: Int
	get() = this.sumOf {
		@Suppress("Useless_Cast")
		when (it.code) {
			in 0xE000..0xE0A8 -> 0xDFFF - it.code
			in 0xE100..0xE1A8 -> -0xE0FF + it.code
			else -> when (it) {
				'i', '!', ',', '.', '\'', ':', ';', '|' -> 2
				'l', '`' -> 3
				'I', 't', ' ', '\"', '(', ')', '*', '[', ']', '{', '}' -> 4
				'k', 'f', '<', '>' -> 5
				'@', '~', '«', '»' -> 7
				else -> 6
			} as Int
		}
	}

/**
 * Append a left shift to a Component
 * @param shift number of pixels to shift between 1 and 169
 */
fun Component.shiftLeft(shift: Int) = if (shift in 1..169) {
	this.append(text((0xDFFF + shift).toChar()).font(SPECIAL_FONT_KEY))
} else this

/**
 * Create a new Component starting with a left shift
 * @param shift number of pixels to shift between 1 and 169
 */
fun newLeftShift(shift: Int): Component = if (shift in 1..169) {
	text((0xDFFF + shift).toChar()).font(SPECIAL_FONT_KEY)
} else empty()

/**
 * Add a left shift to the beginning of a Component equal to the Component's length
 */
fun Component.withLeftShift(): Component = this.withLeftShift(this.plainText().minecraftLength)

/**
 * Add a left shift to the beginning of a Component
 * @param shift number of pixels to shift between 1 and 169
 */
fun Component.withLeftShift(shift: Int): Component = if (shift in 1..169) {
	ofChildren(newLeftShift(shift), this)
} else this

/**
 * Append a right shift to a Component
 * @param shift number of pixels to shift between 1 and 169
 */
fun Component.shiftRight(shift: Int) = if (shift in 1..169) {
	this.append(text((0xE0FF + shift).toChar()).font(SPECIAL_FONT_KEY))
} else this

/**
 * Create a new Component starting with a right shift
 * @param shift number of pixels to shift between 1 and 169
 */
fun newRightShift(shift: Int): Component = if (shift in 1..169) {
	text((0xE0FF + shift).toChar()).font(SPECIAL_FONT_KEY)
} else empty()

/**
 * Add a right shift to the beginning of a Component
 * @param shift number of pixels to shift between 1 and 169
 */
fun Component.withRightShift(shift: Int): Component = if (shift in 1..169) {
	ofChildren(newRightShift(shift), this)
} else this

/**
 * Add a left shift that returns the text to the left (beginning) of the Component
 */
fun Component.shiftToLeftOfComponent(): Component {
	println("${this.plainText()}: ${this.plainText().minecraftLength}")
	return this.shiftLeft(this.plainText().minecraftLength)
}

/**
 * Add a left shift that returns the text to the left (beginning) of the Component, with a right shift offset
 * @param offset the amount of pixels to the right of the left edge to offset by
 */
fun Component.shiftToLeftOfComponent(offset: Int): Component = this.shiftLeft(this.plainText().minecraftLength - offset)

/**
 * Add a right shift that sets the text to the right edge of the GUI
 */
fun Component.shiftToRightGuiEdge(): Component = this.shiftRight(RIGHT_EDGE_SHIFT - this.plainText().minecraftLength)

/**
 * Add a downward shift to the entire Component
 * @param shift number of pixels to shift between 1 and 110
 */
fun Component.shiftDown(shift: Int): Component = if (shift in 1..110) {
	this.font(yFontKey(shift))
} else this

/**
 * Add a downward shift to the entire Component equivalent to the next line
 */
fun Component.shiftToLine(line: Int): Component = this.shiftDown(line * TEXT_HEIGHT)

/**
 * Display a custom GUI background. Assumes that the background is the same width as the Minecraft GUI (176 pixels)
 * @param backgroundChar the character representing the background to display
 */
fun customGuiBackground(backgroundChar: Char) =
	newLeftShift(8).append(text(backgroundChar).color(WHITE).font(SPECIAL_FONT_KEY)).shiftLeft(DEFAULT_GUI_WIDTH)

/**
 * Set the custom GUI header
 * @param header the title of the GUI to be displayed
 */
fun customGuiHeader(header: String) = ofChildren(text(header), newLeftShift(header.minecraftLength))

//</editor-fold>