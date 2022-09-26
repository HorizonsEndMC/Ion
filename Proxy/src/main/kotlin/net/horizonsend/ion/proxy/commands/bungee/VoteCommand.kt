package net.horizonsend.ion.proxy.commands.bungee

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent


@Suppress("Unused")
@CommandAlias("vote|votes|votesites")
class VoteCommand(private val configuration: ProxyConfiguration) : BaseCommand() {

	@Default
	fun onVoteCommand(sender: CommandSender) {
		val siteList = ComponentBuilder("Voting Websites").color(ChatColor.DARK_GREEN)

		PlayerData[sender.name]?.voteTimes?.forEach {
			val siteName = configuration.voteSites[it.key]
			val color = if ((it.value - System.currentTimeMillis()) <= 8400000) { ChatColor.GREEN } else ChatColor.RED

			siteList
				.append(
					ComponentBuilder("$siteName/n")
						.event(HoverEvent(HoverEvent.Action.valueOf(it.key)))
						.event(ClickEvent(ClickEvent.Action.OPEN_URL, it.key))
						.color(color)
						.create()
				)
			}
		sender.sendMessage(*siteList.create())
	}
}