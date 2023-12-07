package net.horizonsend.ion.common.database.schema.misc

import com.mongodb.client.ClientSession
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.result.UpdateResult
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.DbObjectCompanion
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.ProjectedResults
import net.horizonsend.ion.common.database.projected
import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.database.updateAll
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.combine
import org.litote.kmongo.descendingSort
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.id.StringId
import org.litote.kmongo.inc
import org.litote.kmongo.ne
import org.litote.kmongo.projection
import org.litote.kmongo.pull
import org.litote.kmongo.withDocumentClass
import java.time.Instant
import java.util.Date
import java.util.UUID

typealias SLPlayerId = StringId<SLPlayer>
/**
 * @param _id The player's Minecraft UUID
 * @param lastKnownName The last username they logged on to the server with
 * @param lastSeen The last time they were seen online
 * @param settlement The settlement they're current a member of
 * @param nation The nation their settlement is currently in. Needs to be updated whenever the settlement nation updates
 * @param snowflake Their discord unique id
 **/
data class SLPlayer(
	override val _id: SLPlayerId,
	var lastKnownName: String,
	var lastSeen: Date = Date.from(Instant.now()),

	var xp: Int = 0,
	val level: Int = 1,

	var settlement: Oid<Settlement>? = null,
	var nation: Oid<Nation>? = null,

	var snowflake: Long? = null,

	var cryopods: Set<Oid<Cryopod>> = setOf(),
	var selectedCryopod: Oid<Cryopod>? = null,
	var wasKilled: Boolean = false,

	var achievements: Set<String> = setOf(),
	var bounty: Double = 0.0,

	var contactsEnabled: Boolean = true,
	var contactsStarships: Boolean = true,
	var lastStarshipEnabled: Boolean = true,
	var planetsEnabled: Boolean = true,
	var starsEnabled: Boolean = true,
	var beaconsEnabled: Boolean = true,
	var waypointsEnabled: Boolean = true,
	var compactWaypoints: Boolean = true,
) : DbObject {
	companion object : DbObjectCompanion<SLPlayer, SLPlayerId>(
		SLPlayer::class, setup = {
			ensureIndex(SLPlayer::lastKnownName, indexOptions = IndexOptions().textVersion(3))
			ensureIndex(SLPlayer::settlement)
			ensureIndex(SLPlayer::nation)
			ensureIndex(SLPlayer::snowflake)
		}
	) {
		operator fun get(uuid: UUID): SLPlayer? = col.findOneById(uuid.slPlayerId.toString())

		operator fun get(id: SLPlayerId): SLPlayer? = col.findOneById(id.toString())

		operator fun get(snowflake: Long): SLPlayer? = col.findOne(SLPlayer::snowflake eq snowflake)

		operator fun get(name: String): SLPlayer? = col
			.find(getNamePattern(name))
			.descendingSort(SLPlayer::lastSeen)
			.first()

		fun findIdByName(name: String): SLPlayerId? = col.withDocumentClass<Document>()
			.find(getNamePattern(name))
			.descendingSort(SLPlayer::lastSeen)
			.projection(SLPlayer::_id)
			.projected(SLPlayer::_id)
			.firstOrNull()
			?.get(SLPlayer::_id)

		private fun getNamePattern(name: String): Bson {
			val pattern = if (name.startsWith('*')) "^\\$name$" else "^$name$"
			return Filters.regex("lastKnownName", pattern, "i")
		}

		fun getName(id: SLPlayerId): String? = findPropById(id, SLPlayer::lastKnownName)

		fun getXP(id: SLPlayerId): Int? = findPropById(id, SLPlayer::xp)

		fun getLevel(id: SLPlayerId): Int? = findPropById(id, SLPlayer::level)

		fun getXPAndLevel(id: SLPlayerId): Pair<Int, Int>? {
			val results: ProjectedResults = findPropsById(
				id, SLPlayer::xp, SLPlayer::level
			) ?: return null

			val xp: Int = results[SLPlayer::xp]
			val level: Int = results[SLPlayer::level]

			return xp to level
		}

		fun setLevel(id: SLPlayerId, level: Int): UpdateResult =
			updateById(id, org.litote.kmongo.setValue(SLPlayer::level, level))

		fun addXP(id: SLPlayerId, addition: Int): UpdateResult =
			updateById(id, inc(SLPlayer::xp, addition))

		fun setXP(id: SLPlayerId, xp: Int): UpdateResult =
			updateById(id, org.litote.kmongo.setValue(SLPlayer::xp, xp))

		fun isSettlementLeader(slPlayerId: SLPlayerId): Boolean = !Settlement.none(Settlement::leader eq slPlayerId)

		fun isMemberOfSettlement(slPlayerId: SLPlayerId, settlementId: Oid<Settlement>): Boolean =
			matches(slPlayerId, SLPlayer::settlement eq settlementId)

		fun isMemberOfNation(slPlayerId: SLPlayerId, nationId: Oid<Nation>): Boolean =
			matches(slPlayerId, SLPlayer::nation eq nationId)

		private fun isSettlementLeader(sess: ClientSession, slPlayerId: SLPlayerId): Boolean =
			Settlement.col.countDocuments(sess, Settlement::leader eq slPlayerId) != 0L

		fun leaveSettlement(slPlayerId: SLPlayerId): Unit = trx { sess ->
			require(!isSettlementLeader(sess, slPlayerId)) { "$slPlayerId is the leader of their settlement" }

			require(matches(sess, slPlayerId, SLPlayer::settlement ne null)) { "$slPlayerId isn't in a settlement" }

			SettlementRole.col.updateAll(sess, pull(SettlementRole::members, slPlayerId))
			NationRole.col.updateAll(sess, pull(NationRole::members, slPlayerId))

			updateById(
				sess, slPlayerId,
				combine(
					org.litote.kmongo.setValue(SLPlayer::settlement, null),
					org.litote.kmongo.setValue(
						SLPlayer::nation, null
					)
				)
			)
		}

		fun joinSettlement(slPlayerId: SLPlayerId, settlementId: Oid<Settlement>): Unit = trx { sess ->
			// require they're not in a settlement already
			require(matches(sess, slPlayerId, SLPlayer::settlement eq null))

			require(Settlement.exists(sess, settlementId))

			// get the nation the settlement's in
			val nation = Settlement.col.findOneById(sess, settlementId)!!.nation

			Settlement.updateById(sess, settlementId, pull(Settlement::invites, slPlayerId))
			updateById(
				sess, slPlayerId,
				org.litote.kmongo.setValue(SLPlayer::settlement, settlementId),
				org.litote.kmongo.setValue(
					SLPlayer::nation, nation
				)
			)
		}
	}
}
