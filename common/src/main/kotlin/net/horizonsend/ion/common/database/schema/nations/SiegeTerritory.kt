package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import java.time.DayOfWeek
import java.util.Date

class SiegeTerritory(
	override val _id: Oid<SiegeTerritory>,
	var name: String,
	var world: String,
	var x: Int,
	var z: Int,
	var siegeHour: Int,
	var siegeDays: Set<DayOfWeek> = setOf(),
	var kothPoints: MutableMap<Oid<FrontierNation>, Int>,
	var nation: Oid<FrontierNation>? = null,
) : DbObject {

	companion object : OidDbObjectCompanion<SiegeTerritory>(SiegeTerritory::class, setup = {
		ensureUniqueIndex(SiegeTerritory::name)
		ensureUniqueIndex(SiegeTerritory::world, SiegeTerritory::x, SiegeTerritory::z)
	}) {
		fun create(
			name: String,
			world: String,
			x: Int,
			z: Int,
			siegeHour: Int,
			siegeDays: Set<DayOfWeek>,
			kothPoints: MutableMap<Oid<FrontierNation>, Int>,
		): Oid<SiegeTerritory> {
			val id: Oid<SiegeTerritory> = objId()
			col.insertOne(SiegeTerritory(id, name, world, x, z, siegeHour, siegeDays, kothPoints))
			return id
		}

		fun delete(id: Oid<SiegeTerritory>) = trx { sess ->
			SiegeTerritorySiege.col.deleteMany(sess, SiegeTerritory::_id eq id)
			col.deleteOneById(sess, id)
		}
	}
}

data class SiegeTerritorySiege(
	override val _id: Oid<SiegeTerritorySiege>,
	val territory: Oid<SiegeTerritory>,
	val time: Date
) : DbObject {
	companion object : OidDbObjectCompanion<SiegeTerritorySiege>(SiegeTerritorySiege::class, setup = {
		ensureIndex(SiegeTerritorySiege::territory)
	}) {
		fun create(siegeTerritoryId: Oid<SiegeTerritory>): Oid<SiegeTerritorySiege> {
			val id: Oid<SiegeTerritorySiege> = objId()
			col.insertOne(SiegeTerritorySiege(id, siegeTerritoryId, Date(System.currentTimeMillis())))
			return id
		}
	}
}
