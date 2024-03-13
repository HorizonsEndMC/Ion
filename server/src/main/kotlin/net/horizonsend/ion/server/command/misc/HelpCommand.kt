package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.text
import net.horizonsend.ion.server.command.SLCommand
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
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
		INTRO(
			title = text("Getting Started"),
			ofChildren(
				text("Haven guide to trade V1 - By Blackular", BOLD), newline(), newline(),
				text("To level up and progress on Horizons ends you will need to learn the 3 primary ways to earn credits"), newline(),
				text("which are used to purchase XP, print ships and buildings or trade for goods and services.")
			),

			ofChildren(
				text("Commissions - Credits for questing", BOLD), newline(), newline(),
				text("The most common of the ways is the daily commissions which can be started at any OST or trade city"), newline(),
				text("by talking to the Commissions droid, your journal can be checked to see what commission you have.")
			),

			ofChildren(
				text("The commissions are basic fetch quests consisting of mining, NPC killing and travel jobs."),
				text("You can do up to 3 a day and if done daily they will retain the streak from the previous day"),
				text("allowing you to earn a maximum of 15k per commission."),
			),

			ofChildren(
				text("Crating - Shipping from point A to B", BOLD), newline(), newline(),
				text("The second most common is crating. Below level 20 it isn't advised to crate outside seeing how it's done as the time and fuel used are often not worth it at low levels.")
			),
			text("To start you purchase crates at a Trade city or OST which match the capacity of your ship, you then place them on the sticky pistons in the ship until you have no spots left, then redetect and deliver them to their listed location for a small profit."),

			ofChildren(
				text("Trading - Producing and selling goods and services", BOLD), newline(), newline(),
				text("You can also earn credits from players by trading with players in-person or via the Trade cities and OST markets."), newline(),
				text("Mining and selling materials is also another viable option.")
			),
			ofChildren(
				text("As is designing and constructing buildings, ships and other things that people are willing to pay for, another option is farming and selling crops to Prometheus station, a maximum of 30,000c a day can be gained"),
				text("and is located in the Sirius star system."),
				text("Posting a thread in the discords trade section is a good way to advertise your products or selling them via the"),
				text("bazaar commands while at an OST or Trade city will generate you sales and income if you don't overprice your goods.")
			)
		),

		SHOOTING(
			text("Shooting Guide"),
			text("blah blah"),
			text("blah blah 2"),
			text("blah blah 3"),
		)

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
