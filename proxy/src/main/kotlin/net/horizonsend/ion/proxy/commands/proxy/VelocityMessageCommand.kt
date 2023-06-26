package net.horizonsend.ion.proxy.commands.proxy

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.commands.arguments.greedyString
import net.horizonsend.ion.common.commands.dsl.command
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.commands.arguments.player
import net.horizonsend.ion.proxy.sendRichMessage

private val format = { sender: String, receiver: String, msg: String ->
	"<#7f7fff>[<#b8e0d4>$sender <#7f7fff>-> <#b8e0d4>$receiver<#7f7fff>] <white>$msg"
}

private val conversations = mutableMapOf<CommandSource, CommandSource>()

fun messageCommand() =
	command("message", "msg", "m", "gmessage", "gmsg", "gm") {
		val target by player("target").suggest {
			IonProxy.proxy.allPlayers.forEach { suggest(it.username) }
		}

		val message by greedyString("message")

		runs {
			sendMessage(source, target, message)
		}
	}.buildLiterals()

fun replyCommand() =
	command<CommandSource>("reply", "r", "gr") {
		val message by greedyString("message")

		runs {
			val target =
				conversations[source] ?: return@runs source.userError("You aren't talking to anyone!")

			sendMessage(source, target, message)
		}
	}.buildLiterals()

private fun sendMessage(sender: CommandSource, target: CommandSource, message: String) {
	val senderName = (sender as? Player)?.username ?: "CONSOLE"
	val targetName = (target as? Player)?.username ?: "CONSOLE"

	if (message.isEmpty() || message.all { it == ' ' }) return sender.userError("You can't send empty messages!")

	sender.sendRichMessage(format.invoke("me", targetName, message))
	target.sendRichMessage(format.invoke(senderName, "me", message))

	conversations[sender] = target
	conversations[target] = sender
}
