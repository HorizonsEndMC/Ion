package net.horizonsend.ion.proxy.commands.velocity

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.schema.misc.Message
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.messages.MessageState
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.commands.ProxyCommand
import net.horizonsend.ion.proxy.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

@CommandAlias("mail|inbox|messages")
object VelocityMailCommand : ProxyCommand() {
	@Default
	@Subcommand("inbox")
	fun viewInbox(sender: Player, @Optional pageNumber: Int?) {
		PLUGIN.proxy.scheduler.async {
			val unread = Message.findInState(sender.slPlayerId, MessageState.UNREAD)
			val read = Message.findInState(sender.slPlayerId, MessageState.READ)

			val all = setOf(unread, read).flatten()

			sender.sendMessage(template(Component.text("You have {0} unread messages and {1} read messages.", HE_MEDIUM_GRAY), unread.count(), read.count()))
			sender.sendMessage(formatPaginatedMenu(
				entries = all,
				"/mail inbox",
				currentPage = pageNumber ?: 1,
				maxPerPage = 5,
				entryProvider = { message, _ ->
					val subject = message.subjec?.let { ofChildren(GsonComponentSerializer.gson().deserialize(it), space()) } ?: bracketed(Component.text("No Subject"))
					val senderName = GsonComponentSerializer.gson().deserialize(message.senderName)
					val content = GsonComponentSerializer.gson().deserialize(message.content)

					ofChildren(
						senderName, Component.text(" » ", HE_DARK_GRAY), subject, newline(),
						content, newline(),
						getButtonRow(message)
					)
				}
			))
		}
	}

	private fun getButtonRow(message: Message): Component {
		var deleted = false

		return ofChildren(
			bracketed(Component.text("Delete", RED)).clickEvent(ClickEvent.callback {
				PLUGIN.proxy.scheduler.async {
					if (deleted) {
						it.userError("Message already deleted!")
						return@async
					}

					deleted = true
					Message.delete(message._id)
					it.success("Deleted Message")
				}
			}),
			space(),
			bracketed(Component.text("Archive", GOLD)).clickEvent(ClickEvent.callback {
				PLUGIN.proxy.scheduler.async {
					Message.setState(message._id, MessageState.ARCHIVED)
					it.success("Archived Message")
				}
			}),
			space(),
			bracketed(Component.text("Mark Read", GREEN)).clickEvent(ClickEvent.callback {
				PLUGIN.proxy.scheduler.async {
					Message.setState(message._id, MessageState.READ)
					it.success("Marked Message as Read")
				}
			})
		)
	}
}
