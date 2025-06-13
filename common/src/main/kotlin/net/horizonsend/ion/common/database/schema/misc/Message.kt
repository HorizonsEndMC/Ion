package net.horizonsend.ion.common.database.schema.misc

import com.mongodb.client.FindIterable
import com.mongodb.client.result.DeleteResult
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.messages.MessageState
import net.horizonsend.ion.common.utils.text.GsonComponentString
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.`in`
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

data class Message(
	override val _id: Oid<Message>,

	val recipient: SLPlayerId,

	val subjec: GsonComponentString? = null,
	val senderName: GsonComponentString,
	val content: GsonComponentString,

	var state: MessageState = MessageState.UNREAD
) : DbObject {
	companion object : OidDbObjectCompanion<Message>(
		Message::class,
		setup = {
			ensureIndex(Message::recipient)
		}
	) {
		fun send(recipient: SLPlayerId, senderName: GsonComponentString, content: GsonComponentString, subjec: GsonComponentString? = null): Oid<Message> = trx { sess ->
			val id = objId<Message>()

			col.insertOne(sess, Message(_id = id, recipient = recipient, subjec = subjec, senderName = senderName, content = content))

			return@trx id
		}

		fun sendMany(recipients: Iterable<SLPlayerId>, senderName: GsonComponentString, content: GsonComponentString, subjec: GsonComponentString? = null): Set<Oid<Message>> = trx { sess ->
			val ids = mutableSetOf<Oid<Message>>()

			val messages = recipients.map { recipient ->
				val id = objId<Message>()
				ids.add(id)
				Message(_id = id, recipient = recipient, subjec = subjec, senderName = senderName, content = content)
			}

			col.insertMany(sess, messages)

			return@trx ids
		}

		fun delete(messageId: Oid<Message>): DeleteResult = trx { sess ->
			col.deleteOneById(sess, messageId)
		}

		fun setState(messageId: Oid<Message>, newState: MessageState) {
			col.updateOneById(messageId, setValue(Message::state, newState))
		}

		fun findInState(recipient: SLPlayerId, vararg state: MessageState): FindIterable<Message> {
			return col.find(and(Message::recipient eq recipient, Message::state.`in`(state.toSet())))
		}
	}
}
