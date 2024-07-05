package net.horizonsend.ion.proxy.commands.velocity

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.commands.ProxyCommand
import net.horizonsend.ion.proxy.features.cache.PlayerCache
import net.horizonsend.ion.proxy.utils.sendRichMessage
import net.horizonsend.ion.proxy.utils.slPlayerId
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import kotlin.jvm.optionals.getOrNull

private val conversation = mutableMapOf<SLPlayerId, SLPlayerId>()

@CommandAlias("msg|message|tell")
@CommandPermission("ion.message")
object MessageCommand : ProxyCommand() {
	private val format = { sender: String, receiver: String, msg: String ->
		"<#7f7fff>[<#b8e0d4>$sender <#7f7fff>-> <#b8e0d4>$receiver<#7f7fff>] <white>$msg"
	}

	@Default
	@CommandCompletion("@players")
	@Suppress("unused")
	fun onMessage(player: Player, target: String, message: String) {
		val wrapped = WrappedPlayer(player)

		val targetPlayer = PLUGIN.server.getPlayer(target).getOrNull() ?: return wrapped.userError("Target not found!")

		sendMessage(
			WrappedPlayer(player),
			WrappedPlayer(targetPlayer),
			message
		)
	}

	fun sendMessage(sender: WrappedPlayer, recipient: WrappedPlayer, message: String) {
		val cachedRecipient = PlayerCache.getIfOnline(recipient.uniqueId)

		if (cachedRecipient == null) {
			sender.userError("Target not found!")
			return
		}

		val formatted = message.replace("${recipient.name} ", "")

		sender.sendRichMessage(format.invoke("me", recipient.name, formatted))

		// Let the sender see that the message has been sent, but don't send it to the recipient, or set the convo
		if (cachedRecipient.blockedPlayerIDs.contains(sender.slPlayerId)) return

		recipient.sendRichMessage(format.invoke(sender.name, "me", formatted))

		conversation[sender.slPlayerId] = recipient.slPlayerId
		conversation[recipient.slPlayerId] = sender.slPlayerId
	}
}

@CommandAlias("r|reply")
@CommandPermission("ion.message.reply")
object ReplyCommand : ProxyCommand() {
	@Default
	@Suppress("unused")
	fun command(sender: Player, message: String) {
		val wrapped = WrappedPlayer(sender)

		val id = conversation[sender.slPlayerId]?.uuid ?: return wrapped.userError("Theres no one to reply to.")

		val target = PLUGIN.server.getPlayer(id).getOrNull() ?: run {
			wrapped.userError("The person you were talking to is no longer online!")

			return
		}

		MessageCommand.sendMessage(
			WrappedPlayer(sender),
			WrappedPlayer(target),
			message
		)
	}
}
