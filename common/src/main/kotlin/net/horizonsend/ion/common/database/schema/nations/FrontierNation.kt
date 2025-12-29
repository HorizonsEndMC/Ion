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
import net.horizonsend.ion.common.database.schema.economy.BankedItem
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
import org.litote.kmongo.pullAll
import org.litote.kmongo.setValue
import org.litote.kmongo.util.KMongoUtil.idFilterQuery

data class FrontierNation(
	override val _id: Oid<FrontierNation> = objId(),
	var name: String,
	var leader: SLPlayerId,
	var color: Int,
	var territory: Oid<FrontierTerritory>,
	override var balance: Int = 0,
	override var points: Int = 0,
	override var siegable: Boolean = false,
	val invites: MutableSet<SLPlayerId> = mutableSetOf(),
	val availableBuffs: MutableSet<String> = mutableSetOf(),
	val activatedBuffs: MutableSet<String> = mutableSetOf(),
) : DbObject, MoneyHolder, PointsHolder, Siegable {
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

		fun create(name: String, leader: SLPlayerId, color: Int, territory: Oid<FrontierTerritory>): Oid<FrontierNation> = trx { sess ->
			require(none(nameQuery(name)))
			require(FrontierTerritory.matches(sess, territory, FrontierTerritory.unclaimedQuery))
			require(SLPlayer.matches(sess, leader, SLPlayer::frontierNation eq null))

			val id: Oid<FrontierNation> = objId()
			val frontierNation = FrontierNation(id, name, leader, color, territory)

			SLPlayer.col.updateOne(sess, idFilterQuery(leader), setValue(SLPlayer::frontierNation, id))
			FrontierTerritory.col.updateOne(sess, idFilterQuery(territory), setValue(FrontierTerritory::frontierNation, id))
			FrontierTerritory.col.updateOne(sess, idFilterQuery(territory), setValue(FrontierTerritory::isCapital, true))
			col.insertOne(sess, frontierNation)

			return@trx id
		}

		fun delete(frontierNationId: Oid<FrontierNation>): Unit = trx { sess ->
			require(exists(sess, frontierNationId))

			FrontierTerritory.col.updateMany(sess, FrontierTerritory::frontierNation eq frontierNationId,
				setValue(FrontierTerritory::frontierNation, null))
			FrontierTerritory.col.updateMany(sess, FrontierTerritory::frontierNation eq frontierNationId,
				setValue(FrontierTerritory::isCapital, false))

			FrontierNationRole.col.deleteMany(sess, FrontierNationRole::parent eq frontierNationId)

			BankedItem.col.deleteMany(sess, BankedItem::frontierNation eq frontierNationId)

			updateMembers(sess, frontierNationId, setValue(SLPlayer::frontierNation, null))

			col.deleteOne(sess, idFilterQuery(frontierNationId))
		}

		fun deposit(frontierNationId: Oid<FrontierNation>, amount: Int) {
			updateById(frontierNationId, inc(FrontierNation::balance, amount))
		}

		fun withdraw(frontierNationId: Oid<FrontierNation>, amount: Int) {
			updateById(frontierNationId, inc(FrontierNation::balance, -amount))
		}

		fun updatePoints(frontierNationId: Oid<FrontierNation>, amount: Int) {
			updateById(frontierNationId, inc(FrontierNation::points, amount))
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

		fun getTotalPower(frontierNationId: Oid<FrontierNation>): Int = SLPlayer
			.findProp(SLPlayer::frontierNation eq frontierNationId, SLPlayer::power)
			.sum()

		fun setSiegable(frontierNationId: Oid<FrontierNation>, siegable: Boolean) {
			updateById(frontierNationId, setValue(FrontierNation::siegable, siegable))
		}

		fun setLeader(frontierNationId: Oid<FrontierNation>, slPlayerId: SLPlayerId) = trx { sess ->
			require(SLPlayer.matches(sess, slPlayerId, SLPlayer::frontierNation eq frontierNationId))
			updateById(sess, frontierNationId, setValue(FrontierNation::leader, slPlayerId))
		}

		fun getAvailableBuffs(frontierNationId: Oid<FrontierNation>): Set<String>? = trx { _ ->
            return@trx findPropById(frontierNationId, FrontierNation::availableBuffs)?.toSet()
		}

		fun addAvailableBuff(frontierNationId: Oid<FrontierNation>, buffKey: String) = trx { sess ->
			updateById(sess, frontierNationId, addToSet(FrontierNation::availableBuffs, buffKey))
		}

		fun removeAvailableBuff(frontierNationId: Oid<FrontierNation>, buffKey: String) = trx { sess ->
			updateById(sess, frontierNationId, pull(FrontierNation::availableBuffs, buffKey))
		}

		fun removeAllAvailableBuffs(frontierNationId: Oid<FrontierNation>) = trx { sess ->
			updateById(sess, frontierNationId, setValue(FrontierNation::availableBuffs, mutableSetOf()))
		}

		fun getActivatedBuffs(frontierNationId: Oid<FrontierNation>): Set<String>? = trx { _ ->
			return@trx findPropById(frontierNationId, FrontierNation::activatedBuffs)?.toSet()
		}

		fun addActivatedBuff(frontierNationId: Oid<FrontierNation>, buffKey: String) = trx { sess ->
			updateById(sess, frontierNationId, addToSet(FrontierNation::activatedBuffs, buffKey))
		}

		fun removeActivatedBuff(frontierNationId: Oid<FrontierNation>, buffKey: String) = trx { sess ->
			updateById(sess, frontierNationId, pull(FrontierNation::activatedBuffs, buffKey))
		}

		fun removeAllActivatedBuffs(frontierNationId: Oid<FrontierNation>) = trx { sess ->
			updateById(sess, frontierNationId, setValue(FrontierNation::activatedBuffs, mutableSetOf()))
		}
	}
}
