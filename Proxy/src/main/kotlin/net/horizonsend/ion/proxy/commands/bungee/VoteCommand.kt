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

		for (site in configuration.voteSites) {
			val siteTime: Boolean = if (playerData.voteTimes[site.serviceName] != null) { // roundabout way of setting the color to red if null.
				playerData.voteTimes[site.serviceName]!! - System.currentTimeMillis() >= 86400000
			} else false

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