package net.horizonsend.ion.server.database.schema.nations

import com.mongodb.client.ClientSession
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.territories.Territory
import net.horizonsend.ion.server.database.trx
import net.horizonsend.ion.server.database.updateAll
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
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import org.litote.kmongo.util.KMongoUtil.idFilterQuery

/**
 * Referenced on:
 * - Territory (for settlement owner)
 * - Nation (for capital)
 * - Nation (for settlements they invited)
 * - SettlementRole (for parent)
 * - SLPlayer.NationData (for what they're a member of)
 * - SettlementZone (for parent)
 * - SettlementZone (for trusted)
 */

data class Settlement(
	override val _id: Oid<Settlement>,
	/** The territory the settlement resides in. SHOULD NOT BE UPDATED WITHOUT UPDATING ZONES */
	val territory: Oid<Territory>,
	/** The name of the settlement (user-adjustable) */
	var name: String,
	/** The leader of the settlement */
	var leader: SLPlayerId,
	/** The amount of money the settlement has */
	override var balance: Int = 0,
	/** The nation the settlement is in */
	var nation: Oid<Nation>? = null,
	/** The minimum foreign relation a player must have to build in the settlement */
	var minimumBuildAccess: ForeignRelation = ForeignRelation.SETTLEMENT_MEMBER,
	/** List of players the settlement has invited */
	val invites: MutableSet<SLPlayerId> = mutableSetOf(),
	/** Null if it's not a city, unpaid if it hasn't paid its taxes, active if it's an active city */
	var cityState: CityState? = null,
	/** Lets settlement cities set tax percents on cargo trade and bazaars */
	var tradeTax: Double? = null,

	val needsRefund: Boolean = true
) : DbObject, MoneyHolder {
	enum class CityState { UNPAID, ACTIVE }

	companion object : OidDbObjectCompanion<Settlement>(Settlement::class, setup = {
		ensureUniqueIndex(Settlement::territory)
		ensureUniqueIndexCaseInsensitive(Settlement::name, indexOptions = IndexOptions().textVersion(3))
		ensureUniqueIndex(Settlement::leader)
		ensureIndex(Settlement::nation)
		ensureIndex(Settlement::invites)
		ensureIndex(Settlement::cityState)
	}) {
		fun nameQuery(name: String): Bson = Filters.regex("name", "^$name$", "i")

		fun findByName(name: String): Oid<Settlement>? {
			return findOneProp(nameQuery(name), Settlement::_id)
		}

		fun getName(settlementId: Oid<Settlement>): String? = findPropById(settlementId, Settlement::name)

		fun getNation(settlementId: Oid<Settlement>): Oid<Nation>? = findPropById(settlementId, Settlement::nation)

		fun getMembers(settlementId: Oid<Settlement>): MongoIterable<SLPlayerId> = SLPlayer
			.findProp(SLPlayer::settlement eq settlementId, SLPlayer::_id)

		fun isCapital(settlementId: Oid<Settlement>?): Boolean = !Nation.none(Nation::capital eq settlementId)

		fun isInvitedTo(settlementId: Oid<Settlement>, slPlayer: SLPlayerId): Boolean =
			matches(settlementId, Settlement::invites contains slPlayer)

		fun addInvite(settlementId: Oid<Settlement>, slPlayer: SLPlayerId): Unit = trx { sess ->
			require(!SLPlayer.matches(sess, slPlayer, SLPlayer::settlement eq settlementId))
			require(!matches(sess, settlementId, Settlement::invites contains slPlayer))
			updateById(sess, settlementId, addToSet(Settlement::invites, slPlayer))
		}

		fun removeInvite(settlementId: Oid<Settlement>, slPlayer: SLPlayerId): Unit = trx { sess ->
			require(matches(sess, settlementId, Settlement::invites contains slPlayer))
			updateById(sess, settlementId, pull(Settlement::invites, slPlayer))
		}

		private fun updateMembers(session: ClientSession, settlementId: Oid<Settlement>, vararg update: Bson): Long {
			return SLPlayer.col.updateMany(session, SLPlayer::settlement eq settlementId, combine(*update))
				.matchedCount
		}

		fun create(territory: Oid<Territory>, name: String, leader: SLPlayerId): Oid<Settlement> = trx { sess ->
			require(none(sess, nameQuery(name)))
			require(Territory.matches(sess, territory, Territory.unclaimedQuery))
			require(SLPlayer.matches(sess, leader, SLPlayer::settlement eq null))

			val id: Oid<Settlement> =
				objId()
			val settlement = Settlement(id, territory, name, leader, needsRefund = false)

			SLPlayer.col.updateOne(sess, idFilterQuery(leader), setValue(SLPlayer::settlement, id))
			Territory.col.updateOne(
				sess, idFilterQuery(territory),
				setValue(Territory::settlement, id)
			)
			col.insertOne(sess, settlement)

			return@trx id
		}

		fun delete(settlementId: Oid<Settlement>) {
			// leave nation first, to update members that they are no longer in a nation, remove nation roles, etc
			leaveNation(settlementId)

			trx { sess ->
				require(exists(sess, settlementId))

				// make the members no long members
				updateMembers(sess, settlementId, set(SLPlayer::settlement setTo null, SLPlayer::nation setTo null))

				// remove all related settlement roles
				SettlementRole.col.deleteMany(sess, SettlementRole::parent eq settlementId)

				// update the territory's settlement
				Territory.col.updateOne(
					sess, Territory::settlement eq settlementId,
					set(Territory::settlement setTo null, Territory::isProtected setTo false)
				)

				// remove/update all the relevant settlement regions
				SettlementZone.col.deleteMany(sess, SettlementZone::settlement eq settlementId)
				SettlementZone.col.updateMany(
					sess,
					SettlementZone::trustedSettlements ne null,
					pull(SettlementZone::trustedSettlements, settlementId)
				)

				// remove invite from nations
				Nation.col.updateAll(sess, pull(Nation::invites, settlementId))

				// remove the actual settlement
				col.deleteOne(sess, idFilterQuery(settlementId))
			}
		}

		fun leaveNation(settlementId: Oid<Settlement>): Boolean = trx { sess ->
			require(exists(sess, settlementId))
			require(Nation.none(sess, Nation::capital eq settlementId))

			val members: List<SLPlayerId> = getMembers(settlementId).toList()

			// remove roles
			NationRole.col.updateAll(sess, pullAll(NationRole::members, members))

			// unset nation for members
			updateMembers(sess, settlementId, setValue(SLPlayer::nation, null))

			// unset actual settlement nation
			return@trx col.updateOne(
				sess, idFilterQuery(settlementId),
				setValue(Settlement::nation, null)
			).modifiedCount > 0
		}

		fun joinNation(settlementId: Oid<Settlement>, nationId: Oid<Nation>): Unit = trx { sess ->
			require(exists(sess, settlementId))

			// require the settlement isn't already in a nation
			require(matches(sess, settlementId, Settlement::nation eq null))

			// update the nation of all members
			updateMembers(sess, settlementId, setValue(SLPlayer::nation, nationId))

			// set the nation to the new nation
			col.updateOne(sess, idFilterQuery(settlementId), setValue(Settlement::nation, nationId))
		}

		fun deposit(settlementId: Oid<Settlement>, amount: Int) {
			updateById(settlementId, inc(Settlement::balance, amount))
		}

		fun withdraw(settlementId: Oid<Settlement>, amount: Int) {
			updateById(settlementId, inc(Settlement::balance, -amount))
		}

		fun setCityState(settlementId: Oid<Settlement>, cityState: CityState?): Unit = trx { sess ->
			require(exists(sess, settlementId))

			val protected = cityState == CityState.ACTIVE

			col.updateOneById(sess, settlementId, setValue(Settlement::cityState, cityState))
			Territory.col.updateOne(
				sess, Territory::settlement eq settlementId,
				setValue(Territory::isProtected, protected)
			)
		}

		fun setName(settlementId: Oid<Settlement>, name: String): Unit = trx { sess ->
			require(none(sess, and(Settlement::_id ne settlementId, nameQuery(name))))
			updateById(sess, settlementId, setValue(Settlement::name, name))
		}

		fun setLeader(settlementId: Oid<Settlement>, slPlayerId: SLPlayerId): Unit = trx { sess ->
			require(SLPlayer.matches(sess, slPlayerId, SLPlayer::settlement eq settlementId))
			updateById(sess, settlementId, setValue(Settlement::leader, slPlayerId))
		}

		fun setMinBuildAccess(settlementId: Oid<Settlement>, level: ForeignRelation) {
			updateById(settlementId, setValue(Settlement::minimumBuildAccess, level))
		}

		fun setNeedsRefund(settlementId: Oid<Settlement>) {
			updateById(settlementId, setValue(Settlement::needsRefund, false))
		}
	}

	enum class ForeignRelation { NONE, ALLY, NATION_MEMBER, SETTLEMENT_MEMBER, STRICT; }
}
