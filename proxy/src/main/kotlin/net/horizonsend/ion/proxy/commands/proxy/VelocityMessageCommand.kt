package net.horizonsend.ion.proxy.commands.proxy

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.sendRichMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.jvm.optionals.getOrNull


class VelocityMessageCommand : SimpleCommand {
	companion object {
		val format = { sender: String, receiver: String, msg: String ->
			"<#7f7fff>[<#b8e0d4>$sender <#7f7fff>-> <#b8e0d4>$receiver<#7f7fff>] <white>$msg"
		}

		val conversations = mutableMapOf<CommandSource, CommandSource>()

		fun sendMessage(sender: CommandSource, target: CommandSource, message: String) {
			val senderName = (sender as? Player)?.username ?: "CONSOLE"
			val targetName = (target as? Player)?.username ?: "CONSOLE"

			if (message.isEmpty() || message.all { it == ' ' }) return sender.userError("You can't send empty messages!")

			sender.sendRichMessage(format.invoke("me", targetName, message))
			target.sendRichMessage(format.invoke(senderName, "me", message))

			conversations[sender] = target
			conversations[target] = sender
		}
	}

	override fun execute(invocation: SimpleCommand.Invocation) {
		val sender = invocation.source()

		if (invocation.arguments().size < 2)
			return sender.userError("Specify a player!")

		val target = invocation.arguments()[0]!!
		val targetPlayer = IonProxy.proxy.getPlayer(target).getOrNull() ?: return sender.userError("Specify a player!")

		val message =
			invocation.arguments().toList().subList(1, invocation.arguments().size).joinToString(separator = " ")

		sendMessage(sender, targetPlayer, message)
	}

	override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> =
		CompletableFuture.completedFuture(
			if (invocation.arguments().isEmpty()) IonProxy.proxy.allPlayers.map { it.username }
			else IonProxy.proxy.allPlayers.map { it.username }
				.filter { it.lowercase().startsWith(invocation.arguments()[0].lowercase()) }
		)
}

class VelocityReplyCommand : SimpleCommand {
	override fun execute(invocation: SimpleCommand.Invocation) {
		val sender = invocation.source()

		val message = invocation.arguments().joinToString(separator = " ")

		val target =
			VelocityMessageCommand.conversations[sender] ?: return sender.userError("You aren't talking to anyone!")

		VelocityMessageCommand.sendMessage(sender, target, message)
	}
}
