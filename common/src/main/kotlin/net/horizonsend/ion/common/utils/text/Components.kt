package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.extensions.userError
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor

fun Audience.paginatedMessage(
	page: Int,
	header: Component?,
	vararg entries: Component,
	footer: Component? = null,
	entriesText: ((Int, Int, Int) -> Component)? = null,
	entriesPerPage: Int = 10,
	leftButton: Component? = null,
	leftCommand: String,
	rightButton: Component? = null,
	rightCommand: String,
	subFooter: Component? = null
) {
	if (page <= 0) return userError("Page must not be less than or equal to zero!")

	val message = text()

	if (header != null) message
		.append(header)
		.append(newline())

	val min = minOf(entries.size, 0 + (entriesPerPage * (page - 1)))
	val max = minOf(entries.size, entriesPerPage + (entriesPerPage * (page - 1)))

	for (entry in entries.toList().subList(min, max)) {
		message
			.append(entry)
			.append(newline())
	}

	if (footer != null) message
		.append(footer)
		.append(newline())

	if (entriesText != null) message
		.append(entriesText(min, max, entries.size))
		.append(newline())

	val arrowsText = text()
		.append(
			leftButton ?: text("Previous", NamedTextColor.RED)
				.clickEvent(ClickEvent.runCommand("/$leftCommand ${maxOf(1, page - 1)}"))
				.hoverEvent(text("/bounty top ${maxOf(1, page - 1)}"))
		)
		.append(text("  |  ", NamedTextColor.DARK_GRAY))
		.append(
			rightButton ?: text("Next", NamedTextColor.RED)
				.clickEvent(ClickEvent.runCommand("/$rightCommand ${page + 1}"))
				.hoverEvent(text("/bounty top ${page + 1}"))
		)

	message.append(arrowsText)

	if (subFooter != null) message
		.append(subFooter)

	sendMessage(message.build())
}
