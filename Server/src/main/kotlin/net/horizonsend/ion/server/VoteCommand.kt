package net.horizonsend.ion.server

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import org.bukkit.command.CommandSender

@Suppress("Unused")
@CommandAlias("vote")
class VoteCommand : BaseCommand() {
	@Default
	fun onVote(sender: CommandSender) {
		sender.sendRichMessage(
			"<gray><bold>==============</bold><gray>\n"+
			"<bold><green><click:open_url:'https://minecraft-mp.com/server-s306850'><hover:show_text:'<blue><italic>https://minecraft-mp.com/server-s306850<italic><blue>'>Vote site 1</click><green><bold>\n"+
			"<bold><green><click:open_url:'https://minecraft-server-list.com/server/489018/'><hover:show_text:'<blue><italic>https://minecraft-server-list.com/server/489018/<italic><blue>'>Vote site 2</click><green><bold>\n"+
			"<bold><green><click:open_url:'https://www.planetminecraft.com/server/horizon-s-end/'><hover:show_text:'<blue><italic>https://www.planetminecraft.com/server/horizon-s-end/<italic><blue>'>Vote site 3</click><green><bold>\n"+
			"</bold><gray>==============<gray></bold>"
		)
	}
}