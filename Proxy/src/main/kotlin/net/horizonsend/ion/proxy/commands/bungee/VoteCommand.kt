package net.horizonsend.ion.proxy.commands.bungee

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandAlias("vote|votes|votesites")
class VoteCommand(private val configuration: ProxyConfiguration) : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onVoteCommand(sender: ProxiedPlayer) {
		val playerData = PlayerData[sender.uniqueId]

		val siteList = ComponentBuilder("Voting Websites")
			.color(ChatColor.GOLD)
			.underlined(true)

		for ((url, name) in configuration.voteSites) {
			val colour = if ((playerData.voteTimes[url] ?: 0) - System.currentTimeMillis() <= 8_400_000) ChatColor.GREEN else ChatColor.RED

			siteList.append(
				ComponentBuilder("\n\n$name\n")
					.color(ChatColor.YELLOW).underlined(false)
					.append(url).underlined(true)
					.event(ClickEvent(ClickEvent.Action.OPEN_URL, url))
					.color(colour)
					.create()
			)
		}

		sender.sendMessage(*siteList.create())
	}
}