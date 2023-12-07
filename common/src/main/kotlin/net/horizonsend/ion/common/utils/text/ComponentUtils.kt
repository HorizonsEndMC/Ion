package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
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

fun lineBreakWithCenterText(value: Component) = bracketed(
	value,
	text().append(lineBreak(15)).append(text("« ", HE_DARK_GRAY)).build(),
	text().append(text(" »", HE_DARK_GRAY)).append(lineBreak(15)).build()
)

fun lineBreak(width: Int, color: TextColor = HE_DARK_GRAY, vararg decorations: TextDecoration) = text(repeatString("=", width), color, TextDecoration.STRIKETHROUGH, *decorations)

fun bracketed(value: Component, leftBracket: Char = '[', rightBracket: Char = ']', bracketColor: TextColor = HE_DARK_GRAY) =
	ofChildren(text(leftBracket, bracketColor), value, text(rightBracket, bracketColor))

fun bracketed(value: Component, leftBracket: Component, rightBracket: Component) = ofChildren(leftBracket, value, rightBracket)

fun templateMiniMessage(
	message: String,
	paramColor: TextColor = NamedTextColor.WHITE,
	useQuotesAroundObjects: Boolean = true,
	vararg parameters: Any
): Component {
	return template(miniMessage().deserialize(message), paramColor, useQuotesAroundObjects, *parameters)
}

fun template(
	message: String,
	color: TextColor,
	paramColor: TextColor = NamedTextColor.WHITE,
	useQuotesAroundObjects: Boolean = true,
	vararg parameters: Any
): Component {
	return template(text(message, color), paramColor, useQuotesAroundObjects, *parameters)
}

fun template(message: Component, vararg parameters: Any) = template(message, paramColor = NamedTextColor.WHITE, useQuotesAroundObjects = true, *parameters)
fun template(message: Component, paramColor: TextColor, vararg parameters: Any) = template(message, paramColor = paramColor, useQuotesAroundObjects = true, *parameters)

fun template(
	message: Component,
	paramColor: TextColor,
	useQuotesAroundObjects: Boolean,
	vararg parameters: Any
): Component {
	val replacement = TextReplacementConfig.builder()
		.match(Pattern.compile("\\{([0-9]*?)}"))
		.replacement { matched: MatchResult, _ ->
			val index = matched.group().subStringBetween('{', '}').toInt()

			return@replacement when (val param = parameters[index]) {
				is ComponentLike -> param
				is Number -> text(param.toString(), paramColor)
				else -> text(if (useQuotesAroundObjects) { "\"$param\"" } else param.toString(), paramColor)
			}
		}
		.build()

	return message.replaceText(replacement)
}

fun Iterable<ComponentLike>.join(separator: Component = text(", ")): Component {
	val iterator = this.iterator()

	val builder = text()

	while (iterator.hasNext()) {
		builder.append(iterator.next())

		if (iterator.hasNext()) builder.append(separator)
	}

	return builder.build()
}

fun Component.addSpace(prefix: Boolean): Component {
	if ((this as? TextComponent)?.content()?.isEmpty() == true) return this

	val plainText = plainText()

	if (plainText.isEmpty()) return this

	return if (prefix) ofChildren(text(" "), this) else ofChildren(this, text(" "))
}
