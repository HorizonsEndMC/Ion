package net.horizonsend.ion.common.utils.messages

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.Message
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.extensions.CommonPlayer
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

abstract class Inboxes : IonComponent() {
	abstract fun runAsync(task: () -> Unit)
	abstract fun notify(recipient: SLPlayerId, message: Component)

	private fun Component.seralize(): String = GsonComponentSerializer.gson().serialize(this)

	fun sendMessage(content: Component, senderName: Component, subject: Component? = null, vararg recipient: SLPlayerId) {
		sendMessage(content = content, senderName = senderName, subject = subject, recipients = setOf(*recipient))
	}

	fun sendMessage(content: Component, senderName: Component, subject: Component? = null, recipients: Iterable<SLPlayerId>) {
		runAsync {
			Message.sendMany(recipients = recipients, subject = subject?.seralize(), senderName = senderName.seralize(), content = content.seralize())
			val inboxCommand = text("/inbox", WHITE).clickEvent(ClickEvent.runCommand("/inbox")).hoverEvent(text("/inbox"))
			val sentMessage = template(text("You recieved a message from {0}! Use {1} to read it.", HE_MEDIUM_GRAY), senderName, inboxCommand)
			recipients.forEach { notify(it, sentMessage) }
		}
	}

	fun sendToNationMembers(sender: CommonPlayer, nation: Oid<Nation>, content: Component) {
		runAsync {
			val members = Nation.getMembers(nation)
			sendMessage(content = content, senderName = formatNationName(nation), subject = bracketed(text("Nation Broadcast", NamedTextColor.RED), leftBracket = '<', rightBracket = '>'), recipients = members)
		}
	}

	fun sendToSettlementMembers(sender: CommonPlayer, settlement: Oid<Settlement>, content: Component) {
		runAsync {
			val members = Settlement.getMembers(settlement)
			sendMessage(content = content, senderName = formatSettlementName(settlement), subject = bracketed(text("Settlement Broadcast", NamedTextColor.DARK_AQUA), leftBracket = '<', rightBracket = '>'), recipients = members)
		}
	}

	fun sendServerMessage(recipients: Iterable<SLPlayerId>) {

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

			val clickText = bracketed(text("Here", NamedTextColor.WHITE)).clickEvent(ClickEvent.runCommand("/mail inbox"))

			val message = ofChildren(
				template(text("{0} » You have {1} unread message${if (count != 1) "s" else ""}!", HE_MEDIUM_GRAY), HORIZONS_END_BRACKETED, count), Component.newline(),
				template(text("Click {0} to view your inbox.", HE_MEDIUM_GRAY), clickText)
			)

			playerAudience.sendMessage(message)
		}
	}
}
