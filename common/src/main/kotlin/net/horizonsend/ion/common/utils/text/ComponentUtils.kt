package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.horizonsend.ion.common.utils.text.HEColorScheme.Companion.HE_DARK_GRAY
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.util.regex.MatchResult
import java.util.regex.Pattern

// Serialization shortland
fun String.miniMessage() = MiniMessage.miniMessage().deserialize(this)
fun ComponentLike.plainText(): String = PlainTextComponentSerializer.plainText().serialize(this.asComponent())

operator fun Component.plus(other: ComponentLike): Component = this.append(other)

fun ofChildren(vararg children: ComponentLike) = Component.textOfChildren(*children)

// Utility functions
fun Any.toComponent(): Component = text(toString())

/**
 * Formats the number into credit format, so it is rounded to the nearest hundredth,
 * commas are placed every 3 digits to the left of the decimal point,
 * and "C" is placed at the beginning of the string.
 */
fun Number.toCreditComponent(): Component = text("C${toDouble().roundToHundredth().toText()}", NamedTextColor.GOLD)
fun lineBreak(width: Int, color: TextColor = HE_DARK_GRAY, vararg decorations: TextDecoration) = text(repeatString("=", width), color, TextDecoration.STRIKETHROUGH, *decorations)

fun template(
	message: String,
	color: TextColor,
	paramColor: TextColor = NamedTextColor.WHITE,
	vararg parameters: Any
): Component {
	val messageComponent = text(message, color)

	val replacement = TextReplacementConfig.builder()
		.match(Pattern.compile("\\{([0-9]*?)}"))
		.replacement { matched: MatchResult, _ ->
			val index = matched.group().subStringBetween('{', '}').toInt()

			return@replacement when (val param = parameters[index]) {
				is ComponentLike -> param
				is Number -> text(param.toString(), paramColor)
				else -> text("\"$param\"", paramColor)
			}
		}
		.build()

	return messageComponent.replaceText(replacement)
}

fun bracketed(value: Component, startChar: Char = '[', endChar: Char = ']', bracketColor: TextColor = HE_DARK_GRAY) = ofChildren(text(startChar, bracketColor), value, text(endChar, bracketColor))

fun Iterable<ComponentLike>.join(separator: Component = text(", ")): Component {
	val iterator = this.iterator()

	val builder = text()

	while (iterator.hasNext()) {
		builder.append(iterator.next())

		if (iterator.hasNext()) builder.append(separator)
	}

	return builder.build()
}
