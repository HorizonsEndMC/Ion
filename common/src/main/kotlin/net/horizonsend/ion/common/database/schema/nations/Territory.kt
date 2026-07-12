package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.common.database.none
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.economy.CargoCrateShipment
import net.horizonsend.ion.common.database.schema.economy.CityNPC
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.ne
import org.litote.kmongo.or
import org.litote.kmongo.pull
import org.litote.kmongo.util.KMongoUtil.idFilterQuery

/**
 * Referenced on:
 * - Settlement (for the territory they own)
 * - SettlementZone (for the territory of its settlement)
 * - Nation (for the outposts they own)
 * - CargoCrateShipment (for the territory it's from)
 * - CargoCrateShipment (for the territory it's to)
 * - CityNPC (Territory it's in)
 */
data class Territory(
	override val _id: Oid<Territory> = objId(),
	/** The display name of the territory */
	var name: String,
	/** The world the territory is in */
	var world: String,
	/** The data of the points of the territory */
	var polygonData: ByteArray,
	/** The settlement residing here. */
	var settlement: Oid<Settlement>? = null,
	/** The nation with an outpost here. For outposts only, not member settlements! */
	var nation: Oid<Nation>? = null,
	/** The alias given by the nation owner*/
	var alias: String? = null,
	/** The NPC territory owner residing here. */
	var npcOwner: Oid<NPCTerritoryOwner>? = null,
	/** If the territory should be a safe-zone from PVP and explosions */
	var isProtected: Boolean = false,

	/** Trust individual nations, settlements, or players with build permission **/
	var trustedNations: Set<Oid<Nation>> = mutableSetOf(),
	var trustedSettlements: Set<Oid<Settlement>> = mutableSetOf(),
	var trustedPlayers: Set<SLPlayerId> = mutableSetOf(),
) : DbObject {
	// region dumb stuff
	// Use all properties for equals, only id for hashcode
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Territory

		if (_id != other._id) return false
		if (name != other.name) return false
		if (world != other.world) return false
		if (!polygonData.contentEquals(other.polygonData)) return false
		if (settlement != other.settlement) return false
		if (nation != other.nation) return false
		if (npcOwner != other.npcOwner) return false
		if (isProtected != other.isProtected) return false

		return true
	}

	override fun hashCode(): Int {
		return _id.hashCode()
	}
	//endregion

	companion object : OidDbObjectCompanion<Territory>(Territory::class, setup = {
		ensureUniqueIndexCaseInsensitive(Territory::name)
		ensureUniqueIndex(Territory::world, Territory::polygonData)
		ensureIndex(Territory::settlement)
		ensureIndex(Territory::nation)
	}) {
		fun setNation(id: Oid<Territory>, nation: Oid<Nation>?): Unit = trx { sess ->
			if (nation != null) {
				require(matches(sess, id, unclaimedQuery))
				require(Nation.exists(sess, nation))
			}

			if (nation == null) {
				updateById(sess, id, org.litote.kmongo.setValue(Territory::alias, null))
				updateById(sess, id, org.litote.kmongo.setValue(Territory::trustedPlayers, mutableSetOf()))
				updateById(sess, id, org.litote.kmongo.setValue(Territory::trustedSettlements, mutableSetOf()))
				updateById(sess, id, org.litote.kmongo.setValue(Territory::trustedNations, mutableSetOf()))
			}

			updateById(sess, id, org.litote.kmongo.setValue(Territory::nation, nation))
		}

		fun create(name: String, world: String, polygonData: ByteArray): Oid<Territory> = trx { sess ->
			val id = objId<Territory>()
			col.insertOne(sess, Territory(id, name, world, polygonData))
			return@trx id
		}

		// This is actually terrifying, and I hope this never needs to be used
		fun delete(id: Oid<Territory>) = trx { sess ->
			require(exists(sess, id))

			BazaarItem.col.deleteMany(sess, BazaarItem::cityTerritory eq id)
			BazaarOrder.col.deleteMany(sess, BazaarOrder::cityTerritory eq id)
			CargoCrateShipment.col.deleteMany(sess, or(CargoCrateShipment::originTerritory eq id, CargoCrateShipment::destinationTerritory eq id))
			CityNPC.col.deleteMany(sess, CityNPC::territory eq id)
			NPCTerritoryOwner.col.deleteMany(sess, NPCTerritoryOwner::territory eq id)
			Settlement.col.deleteMany(sess, Settlement::territory eq id)
			SettlementZone.col.deleteMany(sess, SettlementZone::territory eq id)
			TradeWorldTerritory.col.deleteMany(sess, TradeWorldTerritory::backingTerritory eq id)

			col.deleteOneById(sess, id)
		}

		fun findByName(name: String): Territory? = trx { sess ->
			col.findOne(sess, Territory::name eq name)
		}

		fun setPolygonData(id: Oid<Territory>, polygonData: ByteArray) = trx { sess ->
			updateById(sess, id, org.litote.kmongo.setValue(Territory::polygonData, polygonData))
		}

		val unclaimedQuery = and(Territory::settlement eq null, Territory::nation eq null, Territory::npcOwner eq null)

		val claimedQuery = or(Territory::settlement ne null, Territory::nation ne null, Territory::npcOwner ne null)

		fun isUnclaimed(territoryId: Oid<Territory>): Boolean =
			col.none(and(idFilterQuery(territoryId), unclaimedQuery))

		fun trustPlayer(settlementId: Oid<Territory>, player: SLPlayerId) {
			updateById(settlementId, addToSet(Territory::trustedPlayers, player))
		}

		fun trustSettlement(settlementId: Oid<Territory>, settlement: Oid<Settlement>) {
			updateById(settlementId, addToSet(Territory::trustedSettlements, settlement))
		}

		fun trustNation(settlementId: Oid<Territory>, nation: Oid<Nation>) {
			updateById(settlementId, addToSet(Territory::trustedNations, nation))
		}

		fun unTrustPlayer(settlementId: Oid<Territory>, player: SLPlayerId) {
			updateById(settlementId, pull(Territory::trustedPlayers, player))
		}

		fun unTrustSettlement(settlementId: Oid<Territory>, settlement: Oid<Settlement>) {
			updateById(settlementId, pull(Territory::trustedSettlements, settlement))
		}

		fun unTrustNation(settlementId: Oid<Territory>, nation: Oid<Nation>) {
			updateById(settlementId, pull(Territory::trustedNations, nation))
		}
	}
}
