package net.horizonsend.ion.common.database.schema.economy

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Territory
import org.litote.kmongo.and
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.inc
import java.util.Date
import java.util.concurrent.TimeUnit

data class
CargoCrateShipment(
    override val _id: Oid<CargoCrateShipment> = objId(),
    /** For outdated ones, this is an int, for newer ones, it's the same as _id */
	val itemStoredId: String,
    val player: SLPlayerId,
    val crate: Oid<CargoCrate>,
    val claimTime: Date,
    val expireTime: Date,
    val originTerritory: Oid<Territory>,
    val destinationTerritory: Oid<Territory>,
    var soldCrates: Int,
    val totalCrates: Int,
    val crateCost: Double,
    val crateRevenue: Double
) : DbObject {
	companion object : OidDbObjectCompanion<CargoCrateShipment>(CargoCrateShipment::class, setup = {
		ensureUniqueIndex(CargoCrateShipment::itemStoredId)
		ensureIndex(CargoCrateShipment::crate)
		ensureIndex(CargoCrateShipment::originTerritory)
		ensureIndex(CargoCrateShipment::destinationTerritory)
	}) {
		fun create(
            player: SLPlayerId,
            crate: Oid<CargoCrate>,
            claimed: Date,
            expires: Date,
            from: Oid<Territory>,
            to: Oid<Territory>,
            total: Int,
            cost: Double,
            revenue: Double
		): Oid<CargoCrateShipment> {
			val id = objId<CargoCrateShipment>()
			val itemId = id.toString()
			col.insertOne(
				CargoCrateShipment(id, itemId, player, crate, claimed, expires, from, to, 0, total, cost, revenue)
			)
			return id
		}

		fun addSold(itemId: String, amount: Int) {
			col.updateOne(CargoCrateShipment::itemStoredId eq itemId, inc(CargoCrateShipment::soldCrates, amount))
		}

		fun getByItemId(itemStoredId: String): CargoCrateShipment? {
			return findOne(CargoCrateShipment::itemStoredId eq itemStoredId)
		}

		fun hasPurchasedFrom(player: SLPlayerId, territory: Oid<Territory>, timeLimitHours: Long): Boolean {
			val cutoffTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(timeLimitHours))
			return !none(
				and(
					CargoCrateShipment::player eq player,
					CargoCrateShipment::originTerritory eq territory,
					CargoCrateShipment::claimTime gte cutoffTime
				)
			)
		}
	}
}
