package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.regex.MatchResult
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * @return A line break of '=' characters on both sides of the provided component
 **/
fun lineBreakWithCenterText(value: ComponentLike, width: Int = 15) = bracketed(
	value,
	ofChildren(lineBreak(width)).append("« ".toComponent(HE_DARK_GRAY)),
	ofChildren(" »".toComponent(HE_DARK_GRAY)).append(lineBreak(width))
)

/**
 * @return A line break of '=' characters the specified width.
 **/
fun lineBreak(width: Int, color: TextColor = HE_DARK_GRAY, vararg decorations: TextDecoration) =
	text(repeatString("=", width), color, TextDecoration.STRIKETHROUGH, *decorations)

/**
 * Puts the provided component between brackets.
 *
 * @sample
 * 	Takes: Value
 * 	Returns [Value]
 **/
fun bracketed(value: ComponentLike, leftBracket: Char = '[', rightBracket: Char = ']', bracketColor: TextColor = HE_DARK_GRAY) =
	bracketed(value, leftBracket.toComponent(bracketColor), rightBracket.toComponent(bracketColor))

/**
 * Puts the provided component between brackets.
 *
 * @sample
 * 	Takes: Value
 * 	Returns [Value]
 **/
fun bracketed(value: ComponentLike, leftBracket: ComponentLike, rightBracket: ComponentLike) = ofChildren(leftBracket, value, rightBracket)

/**
 * Formats the provided MiniMessage text, slotting in the templated locations with their corresponding parameter.
 *
 *
 **/
fun templateMiniMessage(
	message: String,
	paramColor: TextColor = NamedTextColor.WHITE,
	useQuotesAroundObjects: Boolean = true,
	vararg parameters: Any?
): Component {
	return template(MiniMessage.miniMessage().deserialize(message), paramColor, useQuotesAroundObjects, *parameters)
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

fun template(message: Component, useQuotesAroundObjects: Boolean = true, vararg parameters: Any?) =
	template(message, paramColor = NamedTextColor.WHITE, useQuotesAroundObjects = useQuotesAroundObjects, *parameters)

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
				else -> text(
					if (useQuotesAroundObjects) {
						"\"$param\""
					} else param.toString(), paramColor
				)
			}
		}
		.build()

	return message.replaceText(replacement)
}

/** Adds a space to the end of this component if it is not empty */
fun formatSpaceSuffix(message: Component?): Component {
	if (message == null) return Component.empty()
	if (message == Component.empty()) return Component.empty()
	if (message.plainText().isEmpty()) return Component.empty()

	return ofChildren(message, text(" "))
}

/** Adds a space to the start of this component if it is not empty */
fun formatSpacePrefix(message: Component?): Component {
	if (message == null) return Component.empty()
	if (message == Component.empty()) return Component.empty()
	if (message.plainText().isEmpty()) return Component.empty()

	return ofChildren(text(" "), message)
}

const val PAGE_NEXT_BUTTON = " »"
const val PAGE_PREVIOUS_BUTTON = "« "

/**
 * Generates the footer for a paginated menu
 * @see [formatPaginatedMenu]
 **/
fun formatPageFooter(
	command: String,
	currentPage: Int,
	maxPage: Int,
	color: TextColor = HEColorScheme.HE_MEDIUM_GRAY,
	paramColor: TextColor = HEColorScheme.HE_LIGHT_GRAY
): Component {
	val ratio = template(text("Page {0}/{1}", color), paramColor = paramColor, currentPage, maxPage)

	val back = if (currentPage > 1) {
		text(PAGE_PREVIOUS_BUTTON, paramColor)
			.hoverEvent(text("$command ${currentPage - 1}"))
			.clickEvent(ClickEvent.runCommand("$command ${currentPage - 1}"))
	} else Component.empty()
	val forward = if (currentPage != maxPage) {
		text(PAGE_NEXT_BUTTON, paramColor)
			.hoverEvent(text("$command ${currentPage + 1}"))
			.clickEvent(ClickEvent.runCommand("$command ${currentPage + 1}"))
	} else Component.empty()

	val breakdownStart = max(1, currentPage - 2)
	val breakdownMax = min(maxPage, currentPage + 2)

	val breakdown = text()
	breakdown.append(text("(", color))
	if (breakdownStart != 1) breakdown.append(
		ofChildren(
			text(1, paramColor)
				.hoverEvent(text("$command 1"))
				.clickEvent(ClickEvent.runCommand("$command 1")),
			text(" ... ", color)
		)
	)

	val range = (breakdownStart..breakdownMax).iterator()

	while (range.hasNext()) {
		val value = range.next()

		val entry = text(value, paramColor)
			.hoverEvent(text("$command $value"))
			.clickEvent(ClickEvent.runCommand("$command $value"))

		breakdown.append(entry)

		if (range.hasNext()) breakdown.append(text(" | ", color))
	}

	if (breakdownMax != maxPage) breakdown.append(
		ofChildren(
			text(" ... ", color),
			text(maxPage, paramColor)
				.hoverEvent(text("$command $maxPage"))
				.clickEvent(ClickEvent.runCommand("$command $maxPage"))
		)
	)

	breakdown.append(text(")", color))

	return ofChildren(back, ratio, forward, text(" "), breakdown.build())
}

/**
 * Builds a chat paginated menu
 *
 * @param entries The total number of entries of this menu
 * @param command The command used to switch to the next page
 * @param currentPage The current page of the menu
 * @param maxPerPage The number of entries that should be shown per page
 * @param color The color that should be used for the text generated by this function
 * @param paramColor The color that should be used for the text parameters generated by this function
 * @param footerSeparator The optional text that should be inserted between the entries and page selection footer
 * @param entryProvider The method used for generating the components from the source data
 *
 * This method is preferred over generating unused text entries.
 **/
inline fun formatPaginatedMenu(
	entries: Int,
	command: String,
	currentPage: Int,
	maxPerPage: Int = 10,
	color: TextColor = HEColorScheme.HE_MEDIUM_GRAY,
	paramColor: TextColor = HEColorScheme.HE_LIGHT_GRAY,
	footerSeparator: Component? = null,
	entryProvider: (Int) -> Component
): Component {
	val builder = text()

	val min = minOf(entries, 0 + (maxPerPage * (currentPage - 1)))
	val max = minOf(entries, maxPerPage + (maxPerPage * (currentPage - 1)))

	for (entry in min until max) {
		builder.append(ofChildren(entryProvider(entry), Component.newline()))
	}

	val maxPage = ceil(entries.toDouble() / maxPerPage).toInt()

	footerSeparator?.let { builder.append(footerSeparator, Component.newline()) }
	builder.append(formatPageFooter(command, currentPage, maxPage, color, paramColor))

	return builder.build()
}

/**
 * Builds a chat paginated menu
 *
 * @param entries The text entries of this menu
 * @param command The command used to switch to the next page
 * @param currentPage The current page of the menu
 * @param maxPerPage The number of entries that should be shown per page
 * @param color The color that should be used for the text generated by this function
 * @param paramColor The color that should be used for the text parameters generated by this function
 * @param footerSeparator The optional text that should be inserted between the entries and page selection footer
 *
 * This method is not preferred, since many generated entries would go unused.
 **/
fun formatPaginatedMenu(
	entries: List<Component>,
	command: String,
	currentPage: Int,
	maxPerPage: Int = 10,
	color: TextColor = HEColorScheme.HE_MEDIUM_GRAY,
	paramColor: TextColor = HEColorScheme.HE_LIGHT_GRAY,
	footerSeparator: Component? = null,
): Component = formatPaginatedMenu(
	entries.size,
	command,
	currentPage,
	maxPerPage,
	color,
	paramColor,
	footerSeparator
) {
	entries[it]
}
