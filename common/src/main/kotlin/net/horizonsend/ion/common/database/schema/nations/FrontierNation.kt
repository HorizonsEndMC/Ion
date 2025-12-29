package net.horizonsend.ion.common.database.schema.nations

import com.mongodb.client.ClientSession
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.trx
import org.bson.conversions.Bson
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.contains
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.ne
import org.litote.kmongo.pull
import org.litote.kmongo.setValue
import org.litote.kmongo.util.KMongoUtil.idFilterQuery

data class FrontierNation(
	override val _id: Oid<FrontierNation> = objId(),
	var name: String,
	var leader: SLPlayerId,
	var color: Int,
	var world: String,
	var x: Int,
	var z: Int,
	var radius: Int,
	override var balance: Int = 0,
	val invites: MutableSet<SLPlayerId> = mutableSetOf()
) : DbObject, MoneyHolder {
	companion object : OidDbObjectCompanion<FrontierNation>(FrontierNation::class, setup = {
		ensureUniqueIndexCaseInsensitive(FrontierNation::name, indexOptions = IndexOptions().textVersion(3))
		ensureUniqueIndex(FrontierNation::leader)
		ensureIndex(FrontierNation::world)
		ensureIndex(FrontierNation::x)
		ensureIndex(FrontierNation::z)
		ensureIndex(FrontierNation::invites)
	}) {
		private fun nameQuery(name: String) = Filters.regex("name", "^$name$", "i")

		private fun updateMembers(session: ClientSession, frontierNationId: Oid<FrontierNation>, vararg update: Bson): Long {
			return SLPlayer.col.updateMany(session, SLPlayer::frontierNation eq frontierNationId, combine(*update)).matchedCount
		}

		fun create(name: String, leader: SLPlayerId, color: Int, world: String, x: Int, z: Int, radius: Int): Oid<FrontierNation> = trx { sess ->
			require(none(nameQuery(name)))
			require(SLPlayer.matches(sess, leader, SLPlayer::frontierNation eq null))

			val id: Oid<FrontierNation> = objId()
			val frontierNation = FrontierNation(id, name, leader, color, world, x, z, radius)

			SLPlayer.col.updateOne(sess, idFilterQuery(leader), setValue(SLPlayer::frontierNation, id))
			col.insertOne(sess, frontierNation)

			return@trx id
		}

		fun delete(frontierNationId: Oid<FrontierNation>): Unit = trx { sess ->
			require(exists(sess, frontierNationId))

			FrontierNationRole.col.deleteMany(sess, FrontierNationRole::parent eq frontierNationId)

			updateMembers(sess, frontierNationId, setValue(SLPlayer::frontierNation, null))

			col.deleteOne(sess, idFilterQuery(frontierNationId))
		}

		fun deposit(frontierNationId: Oid<FrontierNation>, amount: Int) {
			updateById(frontierNationId, inc(FrontierNation::balance, amount))
		}

		fun withdraw(frontierNationId: Oid<FrontierNation>, amount: Int) {
			updateById(frontierNationId, inc(FrontierNation::balance, -amount))
		}

		fun isInvited(frontierNationId: Oid<FrontierNation>, playerId: SLPlayerId): Boolean {
			return matches(frontierNationId, FrontierNation::invites contains playerId)
		}

		fun addInvite(frontierNationId: Oid<FrontierNation>, playerId: SLPlayerId) {
			updateById(frontierNationId, addToSet(FrontierNation::invites, playerId))
		}

		fun removeInvite(frontierNationId: Oid<FrontierNation>, playerId: SLPlayerId) {
			updateById(frontierNationId, pull(FrontierNation::invites, playerId))
		}

		fun getMembers(frontierNationId: Oid<FrontierNation>): MongoIterable<SLPlayerId> = SLPlayer
			.findProp(SLPlayer::frontierNation eq frontierNationId, SLPlayer::_id)

		fun setName(frontierNationId: Oid<FrontierNation>, newName: String): Unit = trx { sess ->
			require(none(sess, and(FrontierNation::_id ne frontierNationId, nameQuery(newName)))) { "A different frontier nation with that name already exists" }

			updateById(sess, frontierNationId, setValue(FrontierNation::name, newName))
		}

		fun setColor(frontierNationId: Oid<FrontierNation>, rgb: Int) = trx { sess ->
			updateById(sess, frontierNationId, setValue(FrontierNation::color, rgb))
		}

		fun setLocation(frontierNationId: Oid<FrontierNation>, newWorld: String, newX: Int, newZ: Int) = trx { sess ->
			updateById(sess, frontierNationId,
				setValue(FrontierNation::world, newWorld),
				setValue(FrontierNation::x, newX),
				setValue(FrontierNation::z, newZ)
			)
		}

		fun setRadius(frontierNationId: Oid<FrontierNation>, radius: Int) = trx { sess ->
			updateById(sess, frontierNationId, setValue(FrontierNation::radius, radius))
		}
	}
}
