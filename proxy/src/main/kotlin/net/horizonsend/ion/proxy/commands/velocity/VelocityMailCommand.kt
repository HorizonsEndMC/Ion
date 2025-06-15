package net.horizonsend.ion.proxy.commands.velocity

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.schema.misc.Message
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.messages.MessageState
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterTextSpecificWidth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.commands.ProxyCommand
import net.horizonsend.ion.proxy.features.misc.ProxyInbox
import net.horizonsend.ion.proxy.utils.slPlayerId
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

@CommandAlias("mail|inbox|messages")
object VelocityMailCommand : ProxyCommand() {
	@Default
	@Subcommand("inbox")
	fun viewInbox(sender: Player, @Optional specifiedState: MessageState?, @Optional pageNumber: Int?) = asyncCommand(sender) {
		val state = specifiedState ?: MessageState.UNREAD
		val messages = Message.findInState(sender.slPlayerId, state).toList()
		val count = messages.count()

		sender.sendMessage(lineBreakWithCenterTextSpecificWidth(template(text("You have {0} {1} message${if (count != 1) "s" else ""}.", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, count, text(state.name.lowercase())), 240))
		sender.sendMessage(formatPaginatedMenu(
			entries = messages,
			command = "/mail inbox $state",
			currentPage = pageNumber ?: 1,
			maxPerPage = 3,
			footerSeparator = lineBreak(40),
			entryProvider = { message, _ ->
				val subject = message.subjec?.let { ofChildren(GsonComponentSerializer.gson().deserialize(it), space()) } ?: bracketed(text("No Subject"))
				val senderName = GsonComponentSerializer.gson().deserialize(message.senderName)
				val content = GsonComponentSerializer.gson().deserialize(message.content)

				ofChildren(
					senderName, text(" » ", HE_DARK_GRAY), subject, newline(),
					content, newline(),
					getButtonRow(message)
				)
			}
		))
	}

	@Default
	@Subcommand("all|inbox all")
	fun viewInboxAll(sender: Player, @Optional pageNumber: Int?) = asyncCommand(sender) {
		val messages = Message.findInState(sender.slPlayerId, *MessageState.entries.toTypedArray()).toList()
		val count = messages.count()

		sender.sendMessage(lineBreakWithCenterTextSpecificWidth(template(text("You have {0} total message${if (count != 1) "s" else ""}.", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, count), 240))
		sender.sendMessage(formatPaginatedMenu(
			entries = messages,
			command = "/mail inbox all",
			currentPage = pageNumber ?: 1,
			maxPerPage = 3,
			footerSeparator = lineBreak(40),
			entryProvider = { message, _ ->
				val subject = message.subjec?.let { ofChildren(GsonComponentSerializer.gson().deserialize(it), space()) } ?: bracketed(text("No Subject"))
				val senderName = GsonComponentSerializer.gson().deserialize(message.senderName)
				val content = GsonComponentSerializer.gson().deserialize(message.content)

				val state = bracketed(text(message.state.name, WHITE))

				ofChildren(
					state, space(), senderName, text(" » ", HE_DARK_GRAY), subject, newline(),
					content, newline(),
					getButtonRow(message)
				)
			}
		))
	}

	private fun getButtonRow(message: Message): Component {
		return ofChildren(
			bracketed(text("Delete", RED))
				.hoverEvent(text("Delete Message command"))
				.clickEvent(ClickEvent.callback {
					PLUGIN.proxy.scheduler.async {
						val result = Message.delete(message._id)
						if (result.deletedCount == 0L) return@async it.userError("Message not found!")
						it.success("Deleted Message")
					}
				}),
			space(),
			bracketed(text("Archive", GOLD))
				.hoverEvent(text("Archive Message Command"))
				.clickEvent(ClickEvent.callback {
					PLUGIN.proxy.scheduler.async {
						val result = Message.setState(message._id, MessageState.ARCHIVED)
						if (result.matchedCount == 0L) return@async it.userError("Message not found!")
						it.success("Archived Message")
					}
				}),
			space(),
			bracketed(text("Mark Read", GREEN))
				.hoverEvent(text("Mark Read Command"))
				.clickEvent(ClickEvent.callback {
					PLUGIN.proxy.scheduler.async {
						val result = Message.setState(message._id, MessageState.READ)
						if (result.matchedCount == 0L) return@async it.userError("Message not found!")
						it.success("Marked Message as Read")
					}
				})
		)
	}

	@Subcommand("send")
	@CommandCompletion("@allPlayers @nothing")
	fun sendMail(sender: Player, recipientName: String, content: String) = asyncCommand(sender) {
		val player = resolveOfflinePlayer(recipientName)
		val playerName = text().color(WHITE).append(WrappedPlayer(sender).getDisplayName()).build()
		ProxyInbox.sendMessage(recipient = player.slPlayerId, senderName = playerName, subject = null, content = text(content))
		sender.success("Sent message to $recipientName")
	}
}
