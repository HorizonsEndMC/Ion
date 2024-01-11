package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.util.regex.MatchResult
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

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

fun lineBreakWithCenterText(value: Component, width: Int = 15) = bracketed(
	value,
	text().append(lineBreak(width)).append(text("« ", HE_DARK_GRAY)).build(),
	text().append(text(" »", HE_DARK_GRAY)).append(lineBreak(width)).build()
)

fun lineBreak(width: Int, color: TextColor = HE_DARK_GRAY, vararg decorations: TextDecoration) = text(repeatString("=", width), color, TextDecoration.STRIKETHROUGH, *decorations)

fun bracketed(value: Component, leftBracket: Char = '[', rightBracket: Char = ']', bracketColor: TextColor = HE_DARK_GRAY) =
	ofChildren(text(leftBracket, bracketColor), value, text(rightBracket, bracketColor))

fun bracketed(value: Component, leftBracket: Component, rightBracket: Component) = ofChildren(leftBracket, value, rightBracket)

fun templateMiniMessage(
	message: String,
	paramColor: TextColor = NamedTextColor.WHITE,
	useQuotesAroundObjects: Boolean = true,
	vararg parameters: Any?
): Component {
	return template(miniMessage().deserialize(message), paramColor, useQuotesAroundObjects, *parameters)
}

fun template(
	message: String,
	color: TextColor,
	paramColor: TextColor = NamedTextColor.WHITE,
	useQuotesAroundObjects: Boolean = true,
	vararg parameters: Any?
): Component {
	return template(text(message, color), paramColor, useQuotesAroundObjects, *parameters)
}

fun template(message: Component, vararg parameters: Any?) = template(message, paramColor = NamedTextColor.WHITE, useQuotesAroundObjects = true, *parameters)
fun template(message: Component, paramColor: TextColor, vararg parameters: Any?) = template(message, paramColor = paramColor, useQuotesAroundObjects = true, *parameters)
fun template(message: Component, useQuotesAroundObjects: Boolean = true, vararg parameters: Any?) = template(message, paramColor = NamedTextColor.WHITE, useQuotesAroundObjects = useQuotesAroundObjects, *parameters)

fun template(
	message: Component,
	paramColor: TextColor,
	useQuotesAroundObjects: Boolean,
	vararg parameters: Any?
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

fun formatSpaceSuffix(message: Component?): Component {
	if (message == null) return empty()
	if (message == empty()) return empty()
	if (message.plainText().isEmpty()) return empty()

	return ofChildren(message, text(" "))
}

fun formatSpacePrefix(message: Component?): Component {
	if (message == null) return empty()
	if (message == empty()) return empty()
	if (message.plainText().isEmpty()) return empty()

	return ofChildren(text(" "), message)
}

const val PAGE_NEXT_BUTTON = " »"
const val PAGE_PREVIOUS_BUTTON = "« "

fun formatPageFooter(
	command: String,
	currentPage: Int,
	maxPage: Int,
	color: TextColor = HE_MEDIUM_GRAY,
	paramColor: TextColor = HE_LIGHT_GRAY
): Component {
	val ratio = template(text("Page {0}/{1}", color), paramColor = paramColor, currentPage, maxPage)

	val back = if (currentPage > 1) {
		text(PAGE_PREVIOUS_BUTTON, paramColor)
			.hoverEvent(text("$command ${currentPage - 1}"))
			.clickEvent(ClickEvent.runCommand("$command ${currentPage - 1}"))
	} else empty()
	val forward = if (currentPage != maxPage) {
		text(PAGE_NEXT_BUTTON, paramColor)
			.hoverEvent(text("$command ${currentPage + 1}"))
			.clickEvent(ClickEvent.runCommand("$command ${currentPage + 1}"))
	} else empty()

	val breakdownStart = max(1, currentPage - 2)
	val breakdownMax = min(maxPage, currentPage + 2)

	val breakdown = text()
	breakdown.append(text("(", color))
	if (breakdownStart != 1) breakdown.append(ofChildren(
		text(1, paramColor)
			.hoverEvent(text("$command 1"))
			.clickEvent(ClickEvent.runCommand("$command 1")),
		text(" ... ", color)
	))

	val range = (breakdownStart..breakdownMax).iterator()

	while (range.hasNext()) {
		val value = range.next()

		val entry = text(value, paramColor)
			.hoverEvent(text("$command $value"))
			.clickEvent(ClickEvent.runCommand("$command $value"))

		breakdown.append(entry)

		if (range.hasNext()) breakdown.append(text(" | ", color))
	}

	if (breakdownMax != maxPage) breakdown.append(ofChildren(
		text(" ... ", color),
		text(maxPage, paramColor)
			.hoverEvent(text("$command $maxPage"))
			.clickEvent(ClickEvent.runCommand("$command $maxPage"))
	))

	breakdown.append(text(")", color))

	return ofChildren(back, ratio, forward, text(" "), breakdown.build())
}

inline fun formatPaginatedMenu(
	entries: Int,
	command: String,
	currentPage: Int,
	maxPerPage: Int = 10,
	color: TextColor = HE_MEDIUM_GRAY,
	paramColor: TextColor = HE_LIGHT_GRAY,
	footerSeparator: Component? = null,
	transform: (Int) -> Component
): Component {
	val builder = text()

	val min = minOf(entries, 0 + (maxPerPage * (currentPage - 1)))
	val max = minOf(entries, maxPerPage + (maxPerPage * (currentPage - 1)))

	for (entry in min until max) {
		builder.append(ofChildren(transform(entry), newline()))
	}

	val maxPage = ceil(entries.toDouble() / maxPerPage).toInt()

	footerSeparator?.let { builder.append(footerSeparator, newline()) }
	builder.append(formatPageFooter(command, currentPage, maxPage, color, paramColor))

	return builder.build()
}

fun formatPaginatedMenu(
	entries: List<Component>,
	command: String,
	currentPage: Int,
	maxPerPage: Int = 10,
	color: TextColor = HE_MEDIUM_GRAY,
	paramColor: TextColor = HE_LIGHT_GRAY,
	footerSeparator: Component? = null,
): Component {
	val builder = text()

	val min = minOf(entries.size, 0 + (maxPerPage * (currentPage - 1)))
	val max = minOf(entries.size, maxPerPage + (maxPerPage * (currentPage - 1)))

	val sublist = entries
		.subList(min, max)
		.toTypedArray()

	for (entry in sublist) {
		builder.append(ofChildren(entry, newline()))
	}

	val maxPage = ceil(entries.size.toDouble() / maxPerPage).toInt()

	footerSeparator?.let { builder.append(footerSeparator, newline()) }
	builder.append(formatPageFooter(command, currentPage, maxPage, color, paramColor))

	return builder.build()
}
