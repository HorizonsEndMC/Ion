package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

// Serialization
/** Skip building the serializer */
val miniMessage = MiniMessage.miniMessage()

/** Converts the provided MiniMessage string to a component using the MiniMessage serializer. */
fun String.miniMessage() = miniMessage.deserialize(this)

/** Skip building the serializer */
val plainText = PlainTextComponentSerializer.plainText()

/** Converts the provided Component to a string using the PlainText serializer. */
fun ComponentLike.plainText(): String = plainText.serialize(this.asComponent())

// Component manipulation
operator fun Component.plus(other: ComponentLike): Component = this.append(other)

/** Shorthand for Component#textOfChildren */
fun ofChildren(vararg children: ComponentLike) = Component.textOfChildren(*children)

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

fun text(string: String, decoration: TextDecoration): Component = text(string, style(decoration))

/** Analogue of Any#toString */
fun Any.toComponent(color: TextColor = WHITE, vararg decorations: TextDecoration): Component = text(toString(), color, *decorations)

/** Returns an empty component if the provided component was null */
fun Component?.orEmpty(): Component = this ?: empty()

// Shortcuts

// Allow static imports
val OBFUSCATED = TextDecoration.OBFUSCATED
val BOLD = TextDecoration.BOLD
val STRIKETHROUGH = TextDecoration.STRIKETHROUGH
val UNDERLINED = TextDecoration.UNDERLINED
val ITALIC = TextDecoration.ITALIC

val HORIZONS_END = text("Horizon's End", HE_LIGHT_GRAY, BOLD)
val HORIZONS_END_BRACKETED = bracketed(text("Horizon's End", HE_LIGHT_GRAY, BOLD))

// Audience utils
fun Audience.sendMessage(vararg message: Component) {
	sendMessage(ofChildren(*message))
}

fun Collection<Audience>.sendMessage(message: ComponentLike) = ForwardingAudience { this }.sendMessage(message)
