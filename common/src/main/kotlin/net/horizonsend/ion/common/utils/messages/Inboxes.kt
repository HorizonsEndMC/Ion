package net.horizonsend.ion.common.utils.messages

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.Message
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.utils.text.HORIZONS_END_BRACKETED
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.formatSettlementName
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.litote.kmongo.and
import org.litote.kmongo.eq

abstract class Inboxes : IonComponent() {
	abstract fun runAsync(task: () -> Unit)
	abstract fun notify(recipient: SLPlayerId, message: Component)

	fun sendMessages(vararg recipient: SLPlayerId, senderName: Component, subject: Component? = null, content: Component) {
		sendMessages(recipients = setOf(*recipient), senderName = senderName, subject = subject, content = content)
	}

	fun sendMessages(recipients: Iterable<SLPlayerId>, senderName: Component, subject: Component? = null, content: Component) {
		runAsync {
			Message.sendMany(recipients = recipients, subject = subject?.serialize(), senderName = senderName.serialize(), content = content.serialize())
			val inboxCommand = text("/mail", WHITE).clickEvent(ClickEvent.runCommand("/mail")).hoverEvent(text("/mail"))
			val sentMessage = template(text("You recieved a message from {0}! Use {1} to read it.", HE_MEDIUM_GRAY), senderName, inboxCommand)
			recipients.forEach { notify(it, sentMessage) }
		}
	}

	fun sendMessage(recipient: SLPlayerId, senderName: Component, subject: Component? = null, content: Component) {
		runAsync {
			Message.send(recipient = recipient, subject = subject?.serialize(), senderName = senderName.serialize(), content = content.serialize())
			val inboxCommand = text("/mail", WHITE).clickEvent(ClickEvent.runCommand("/mail")).hoverEvent(text("/mail"))
			val sentMessage = template(text("You recieved a message from {0}! Use {1} to read it.", HE_MEDIUM_GRAY), senderName, inboxCommand)
			notify(recipient, sentMessage)
		}
	}

	fun sendToNationMembers(nation: Oid<Nation>, content: Component) {
		runAsync {
			val members = Nation.getMembers(nation)
			sendMessages(
				recipients = members,
				senderName = formatNationName(nation),
				subject = bracketed(text("Nation Broadcast", NamedTextColor.RED), leftBracket = '<', rightBracket = '>'),
				content = content
			)
		}
	}

	fun sendToSettlementMembers(settlement: Oid<Settlement>, content: Component) {
		runAsync {
			val members = Settlement.getMembers(settlement)
			sendMessages(
				recipients = members,
				senderName = formatSettlementName(settlement),
				subject = bracketed(text("Settlement Broadcast", NamedTextColor.DARK_AQUA), leftBracket = '<', rightBracket = '>'),
				content = content
			)
		}
	}

	fun sendServerMessage(recipient: SLPlayerId, subject: Component?, content: Component) {
		sendMessage(recipient, HORIZONS_END_BRACKETED, subject, content)
	}

	fun sendServerMessages(recipients: Iterable<SLPlayerId>, subject: Component?, content: Component) {
		sendMessages(recipients, HORIZONS_END_BRACKETED, subject, content)
	}

	fun markRead(messageId: Oid<Message>) {
		runAsync {
			Message.setState(messageId, MessageState.READ)
		}
	}

	fun archiveMessage(messageId: Oid<Message>) {
		runAsync {
			Message.setState(messageId, MessageState.ARCHIVED)
		}
	}

	fun sendInboxBreakdown(player: SLPlayerId, playerAudience: Audience) {
		runAsync {
			val unread = Message.findInState(player, MessageState.UNREAD)
			val count = unread.count()

			val clickText = bracketed(text("Here", WHITE)).clickEvent(ClickEvent.runCommand("/mail inbox"))

			val message = ofChildren(
				template(text("{0} » You have {1} unread message${if (count != 1) "s" else ""}!", HE_MEDIUM_GRAY), HORIZONS_END_BRACKETED, count), Component.newline(),
				template(text("Click {0} to view your inbox.", HE_MEDIUM_GRAY), clickText)
			)

			playerAudience.sendMessage(message)
		}
	}

	companion object {
		private fun Component.serialize(): String = GsonComponentSerializer.gson().serialize(this)
	}

	fun serverMessage(senderName: Component, body: Component): MessageBuilder = MessageBuilder(this, senderName, body)
	fun settlementMessage(id: Oid<Settlement>, body: Component): MessageBuilder = MessageBuilder(this, formatSettlementName(id), body, Settlement.getMembers(id).toMutableSet())
	fun nationMessage(id: Oid<Nation>, body: Component): MessageBuilder = MessageBuilder(this, formatNationName(id), body, Nation.getMembers(id).toMutableSet())

	class MessageBuilder(
		private val inboxes: Inboxes,
		private var senderName: Component,
		private var body: Component,
		private var recipients: MutableSet<SLPlayerId> = mutableSetOf(),
		private var subject: Component? = null,
		private var allowDuplicates: Boolean = false
	) {
		fun send() {
			inboxes.runAsync {
				val matchingPlayers = if (allowDuplicates) recipients
				else recipients.filterTo(mutableSetOf()) { Message.none(and(Message::recipient eq it, Message::subjec eq subject?.serialize(), Message::content eq body.serialize())) }

				inboxes.sendMessages(matchingPlayers, senderName, subject, body)
			}
		}

		fun addRecipients(recipients: Iterable<SLPlayerId>): MessageBuilder {
			this.recipients.addAll(recipients)
			return this
		}

		fun addRecipient(recipient: SLPlayerId): MessageBuilder {
			this.recipients.add(recipient)
			return this
		}

		fun setSubject(subject: Component?): MessageBuilder {
			this.subject = subject
			return this
		}

		fun disallowDuplicates(): MessageBuilder {
			this.allowDuplicates = false
			return this
		}

		fun setAllowDuplicates(allowDuplicates: Boolean): MessageBuilder {
			this.allowDuplicates = allowDuplicates
			return this
		}

		fun filterRecipients(filter: (SLPlayerId) -> Boolean): MessageBuilder {
			this.recipients = recipients.filterTo(mutableSetOf(), filter)
			return this
		}
	}
}
