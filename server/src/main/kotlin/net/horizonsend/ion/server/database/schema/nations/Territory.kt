package net.horizonsend.ion.server.database.schema.nations

import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.none
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.trx
import org.litote.kmongo.and
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.or
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
	override var world: String,
	/** The data of the points of the territory */
	override var polygonData: ByteArray,
	/** The settlement residing here. */
	var settlement: Oid<Settlement>? = null,
	/** The nation with an outpost here. For outposts only, not member settlements! */
	var nation: Oid<Nation>? = null,
	/** The NPC territory owner residing here. */
	var npcOwner: Oid<NPCTerritoryOwner>? = null,
	/** If the territory should be a safe-zone from PVP and explosions */
	var isProtected: Boolean = false,
) : TerritoryInterface {
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

	companion object : AbstractTerritoryCompanion<Territory>(
		Territory::class,
		Territory::name,
		Territory::world,
		Territory::polygonData,
		setup = {
			ensureIndex(Territory::settlement)
			ensureIndex(Territory::nation)
		}
	) {
		fun setNation(id: Oid<Territory>, nation: Oid<Nation>?): Unit = trx { sess ->
			if (nation != null) {
				require(matches(sess, id, unclaimedQuery))
				require(Nation.exists(sess, nation))
			}
			updateById(sess, id, org.litote.kmongo.setValue(Territory::nation, nation))
		}

		override fun new(id: Oid<Territory>, name: String, world: String, polygonData: ByteArray): Territory =
			Territory(id, name, world, polygonData)


		val unclaimedQuery = and(Territory::settlement eq null, Territory::nation eq null, Territory::npcOwner eq null)

		val claimedQuery = or(Territory::settlement ne null, Territory::nation ne null, Territory::npcOwner ne null)

		fun isUnclaimed(territoryId: Oid<Territory>): Boolean =
			col.none(and(idFilterQuery(territoryId), unclaimedQuery))
	}
}
