package net.starlegacy.database.schema.misc

import com.mongodb.client.ClientSession
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.result.UpdateResult
import java.time.Instant
import java.util.Date
import java.util.UUID
import net.starlegacy.database.DbObject
import net.starlegacy.database.DbObjectCompanion
import net.starlegacy.database.Oid
import net.starlegacy.database.ProjectedResults
import net.starlegacy.database.projected
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRole
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.SettlementRole
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.trx
import net.starlegacy.database.updateAll
import org.bson.Document
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.litote.kmongo.addEachToSet
import org.litote.kmongo.combine
import org.litote.kmongo.descendingSort
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.id.StringId
import org.litote.kmongo.inc
import org.litote.kmongo.ne
import org.litote.kmongo.projection
import org.litote.kmongo.pull
import org.litote.kmongo.withDocumentClass

typealias SLPlayerId = StringId<SLPlayer>

data class SLPlayer(
	/** The player's Minecraft UUID */
	override val _id: SLPlayerId,
	/** The last username they logged on to the server with */
	var lastKnownName: String,
	/** The last time they were seen online */
	var lastSeen: Date = Date.from(Instant.now()),
	var xp: Int = 0,
	val level: Int = 1,
	val unlockedAdvancements: List<String> = listOf(),
	/** The settlement they're current a member of */
	var settlement: Oid<Settlement>? = null,
	/** The nation their settlement is currently in. Needs to be updated whenever the settlement nation updates. */
	var nation: Oid<Nation>? = null
) : DbObject {
	companion object : DbObjectCompanion<SLPlayer, SLPlayerId>(
		SLPlayer::class, setup = {
			ensureIndex(SLPlayer::lastKnownName, indexOptions = IndexOptions().textVersion(3))
			ensureIndex(SLPlayer::settlement)
			ensureIndex(SLPlayer::nation)
		}) {
		operator fun get(uuid: UUID): SLPlayer? = col.findOneById(uuid.slPlayerId.toString())

		operator fun get(id: SLPlayerId): SLPlayer? = col.findOneById(id.toString())

		operator fun get(player: Player): SLPlayer = get(
			player.uniqueId
		) ?: error("Missing SLPlayer for online player ${player.name}")

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

		fun addAdvancement(id: SLPlayerId, vararg advancements: String): UpdateResult =
			updateById(id, addEachToSet(SLPlayer::unlockedAdvancements, advancements.toList()))

		fun removeAdvancement(id: SLPlayerId, advancement: String): UpdateResult =
			updateById(id, pull(SLPlayer::unlockedAdvancements, advancement))

		fun removeAdvancementGlobally(advancement: String): UpdateResult =
			col.updateAll(pull(SLPlayer::unlockedAdvancements, advancement))

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
				sess, slPlayerId, combine(
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
