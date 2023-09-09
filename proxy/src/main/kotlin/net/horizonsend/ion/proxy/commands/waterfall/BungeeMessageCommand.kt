package net.horizonsend.ion.proxy.commands.waterfall

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.proxy.sendRichMessage
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ComponentBuilder

private val convo = mutableMapOf<CommandSender, CommandSender>()

@CommandAlias("msg|message|tell")
class MessageCommand : BaseCommand() {
	private val format = { sender: String, receiver: String, msg: String ->
		"<#7f7fff>[<#b8e0d4>$sender <#7f7fff>-> <#b8e0d4>$receiver<#7f7fff>] <white>$msg"
	}

	@Default
	@CommandCompletion("@players")
	fun command(
		player: CommandSender,
		target: String,
		message: String
	) {
		val target = ProxyServer.getInstance().getPlayer(target) ?: run {
			player.sendMessage(
				*ComponentBuilder()
					.append("Target not found!")
					.color(ChatColor.RED)
					.create()
			)

			return
		}

		val message = message.replace("${target.name} ", "")

		player.sendRichMessage(format.invoke("me", target.name, message))
		target.sendRichMessage(format.invoke(player.name, "me", message))

		convo[player] = target
		convo[target] = player
	}
}

@CommandAlias("r|reply")
class ReplyCommand : BaseCommand() {
	@Default
	fun command(
		player: CommandSender,
		message: String
	) {
		val target = convo[player] ?: run {
			player.sendRichMessage("<#ff7f3f>You aren't talking to anyone!")
			return
		}

		ProxyServer.getInstance().pluginManager.dispatchCommand(
			player,
			"msg ${target.name} $message"
		)
	}
}
