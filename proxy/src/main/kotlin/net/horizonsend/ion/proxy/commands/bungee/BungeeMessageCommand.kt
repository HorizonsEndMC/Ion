package net.horizonsend.ion.proxy.commands.bungee

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.proxy.sendRichMessage
import net.horizonsend.ion.proxy.utils.slPlayerId
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

private val conversation = mutableMapOf<SLPlayerId, SLPlayerId>()

@CommandAlias("msg|message|tell")
class MessageCommand : BaseCommand() {
	private val format = { sender: String, receiver: String, msg: String ->
		"<#7f7fff>[<#b8e0d4>$sender <#7f7fff>-> <#b8e0d4>$receiver<#7f7fff>] <white>$msg"
	}

	@Default
	@CommandCompletion("@players")
	@Suppress("unused")
	fun command(player: ProxiedPlayer, target: String, message: String) {
		val wrapped = WrappedPlayer(player)

		val targetPlayer = ProxyServer.getInstance().getPlayer(target) ?: return wrapped.userError("Target not found!")
//		val cached = PlayerCache[targetPlayer.slPlayerId]

		val formatted = message.replace("${targetPlayer.name} ", "")

		player.sendRichMessage(format.invoke("me", targetPlayer.name, formatted))

		// Let the sender see that the message has been sent, but don't send it to the recipient, or set the convo
//		if (cached.blockedPlayerIDs.contains(player.slPlayerId)) return

		targetPlayer.sendRichMessage(format.invoke(player.name, "me", formatted))

		conversation[player.slPlayerId] = targetPlayer.slPlayerId
		conversation[targetPlayer.slPlayerId] = player.slPlayerId
	}
}

@CommandAlias("r|reply")
class ReplyCommand : BaseCommand() {
	@Default
	@Suppress("unused")
	fun command(player: ProxiedPlayer, message: String) {
		val wrapped = WrappedPlayer(player)

		val id = conversation[player.slPlayerId]?.uuid ?: return wrapped.userError("Theres no one to reply to.")

		val target = ProxyServer.getInstance().getPlayer(id) ?: run {
			wrapped.userError("The person you were talking to is no longer online!")

			return
		}

		ProxyServer.getInstance().pluginManager.dispatchCommand(
			player,
			"msg ${target.name} $message"
		)
	}
}
