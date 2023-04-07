package net.horizonsend.ion.proxy.commands.waterfall

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

@CommandAlias("vote|votes|votesites")
class VoteCommand(private val configuration: ProxyConfiguration) : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onVoteCommand(sender: ProxiedPlayer) = transaction {
		val playerData = PlayerData[sender.uniqueId]!!

		val siteList = ComponentBuilder("Voting Websites")
			.color(ChatColor.GOLD)
			.underlined(true)

		for (site in configuration.voteSites) {
			val dateTime = playerData.voteTimes.find { it.serviceName == site.serviceName }?.dateTime ?: LocalDateTime.now()
			val siteTime = dateTime.isBefore(LocalDateTime.now().minusDays(1))

			val color = if (siteTime) ChatColor.GREEN else ChatColor.RED

			siteList.append(
				ComponentBuilder("\n\n${site.displayName}\n")
					.color(ChatColor.YELLOW).underlined(false)
					.append(site.displayAddress).underlined(true)
					.event(ClickEvent(ClickEvent.Action.OPEN_URL, site.displayAddress))
					.color(color)
					.create()
			)
		}

		sender.sendMessage(*siteList.create())
	}
}
