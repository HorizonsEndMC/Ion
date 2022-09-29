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
import net.md_5.bungee.api.chat.hover.content.Text


@Suppress("Unused")
@CommandAlias("vote|votes|votesites")
class VoteCommand(private val configuration: ProxyConfiguration) : BaseCommand() {
	@Default
	fun onVoteCommand(sender: CommandSender) {
		val siteList = ComponentBuilder("Voting Websites" + ChatColor.GOLD).color(ChatColor.GOLD).underlined(true)

		configuration.voteSites.forEach {
			siteList.append(
					ComponentBuilder("\n\n"+ it.value + "\n")
						.color(ChatColor.YELLOW).underlined(false)
						.append(it.key + "\n").underlined(true)
						.event(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(it.key)))
						.event(ClickEvent(ClickEvent.Action.OPEN_URL, it.key))
						//Get last vote time from current configuration value.
						.color(if (PlayerData[sender.name]?.voteTimes?.containsKey(it.toString()) == true) {
							if ((PlayerData[sender.name]?.voteTimes?.getValue(it.key)?.minus(System.currentTimeMillis()))!! <= 8400000)
						//Color chat red if >24 hours.
						{ ChatColor.GREEN } else ChatColor.RED
						} else {ChatColor.RED})
						.create()
				)
			}

		sender.sendMessage(*siteList.create())
	}
}