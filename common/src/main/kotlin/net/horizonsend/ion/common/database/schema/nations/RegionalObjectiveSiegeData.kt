package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import java.util.Date

data class RegionalObjectiveSiegeData(
	override val _id: Oid<RegionalObjectiveSiegeData> = objId(),
	val objective: Oid<RegionalObjective>,
	val winner: Oid<Nation>,
	val siegeTime: Date = Date(System.currentTimeMillis()),
	val passive: Boolean = false,
	val availableRewards: MutableMap<String, Int> = mutableMapOf()
) : DbObject {
	companion object : OidDbObjectCompanion<RegionalObjectiveSiegeData>(RegionalObjectiveSiegeData::class, setup = {
		ensureIndex(RegionalObjectiveSiegeData::objective)
		ensureIndex(RegionalObjectiveSiegeData::winner)
	}) {
		fun create(
			objective: Oid<RegionalObjective>,
			winner: Oid<Nation>,
			rewards: MutableMap<String, Int> = mutableMapOf(),
			passive: Boolean = false
		): Oid<RegionalObjectiveSiegeData> {
			val id = objId<RegionalObjectiveSiegeData>()
			col.insertOne(RegionalObjectiveSiegeData(id, objective, winner, availableRewards = rewards, passive = passive))
			return id
		}

		fun findByNation(nation: Oid<Nation>) = find(RegionalObjectiveSiegeData::winner eq nation)
	}
}
