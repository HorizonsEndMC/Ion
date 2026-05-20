package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import java.util.Date

data class GasDepotSiegeData(
	override val _id: Oid<GasDepotSiegeData>,
	val depot: Oid<GasDepot>,
	val winner: Oid<Nation>,
	val siegeTime: Date = Date(System.currentTimeMillis()),
	val availableRewards: MutableMap<String, Int> = mutableMapOf()
) : DbObject {
	companion object : OidDbObjectCompanion<GasDepotSiegeData>(GasDepotSiegeData::class, setup = {
		ensureIndex(GasDepotSiegeData::depot)
		ensureIndex(GasDepotSiegeData::winner)
	}) {
		fun create(depot: Oid<GasDepot>, winner: Oid<Nation>, availableRewards: MutableMap<String, Int>): Oid<GasDepotSiegeData> {
			val id = objId<GasDepotSiegeData>()
			col.insertOne(GasDepotSiegeData(id, depot, winner, availableRewards = availableRewards))
			return id
		}

		fun findByNation(nation: Oid<Nation>) = find(GasDepotSiegeData::winner eq nation)
	}
}
