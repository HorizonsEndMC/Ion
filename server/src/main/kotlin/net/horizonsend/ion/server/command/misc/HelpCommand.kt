package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.server.command.SLCommand
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("help")
object HelpCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun onHelp(sender: CommandSender, page: HelpPage, @Optional pageNumber: Int? = null) {
		page.send(sender, pageNumber ?: 1)
	}

	@Subcommand("book")
	@Suppress("unused")
	fun onHelpBook(sender: Player, page: HelpPage) {
		page.book(sender)
	}

	@Suppress("unused")
	enum class HelpPage(val title: Component, private vararg val pages: Component) {

		;

		val author: Component = text("Horizon's End", NamedTextColor.WHITE).decorate(TextDecoration.BOLD)

		fun book(player: Player) = player.openBook(Book.book(title, author, *pages))

		fun send(sender: CommandSender, page: Int) {
			sender.sendMessage(formatPaginatedMenu(
				entries = pages.toList(),
				command = "/help $name",
				currentPage = page,
				maxPerPage = 1
			))
		}
	}
}
