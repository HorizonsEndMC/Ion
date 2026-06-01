package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue
import java.util.Date

enum class RegionalObjectiveType {
	GAS_DEPOT,
	TAX_BEACON
}

data class RegionalObjective(
	override val _id: Oid<RegionalObjective> = objId(),
	var name: String,
	var world: String,
	var x: Int,
	var z: Int,
	var type: RegionalObjectiveType,
	var nation: Oid<Nation>? = null,
	var lastSieged: Date? = null
) : DbObject {
	companion object : OidDbObjectCompanion<RegionalObjective>(RegionalObjective::class, setup = {
		ensureUniqueIndex(RegionalObjective::name, RegionalObjective::type)
		ensureUniqueIndex(RegionalObjective::world, RegionalObjective::x, RegionalObjective::z)
		ensureIndex(RegionalObjective::type)
		ensureIndex(RegionalObjective::nation)
	}) {
		fun create(name: String, world: String, x: Int, z: Int, type: RegionalObjectiveType): Oid<RegionalObjective> = trx { sess ->
			val id = objId<RegionalObjective>()
			col.insertOne(sess, RegionalObjective(id, name, world, x, z, type))
			return@trx id
		}

		fun findByName(name: String, type: RegionalObjectiveType): RegionalObjective? = trx { sess ->
			col.findOne(sess, RegionalObjective::name eq name, RegionalObjective::type eq type)
		}

		fun findByWorld(world: String, type: RegionalObjectiveType): List<RegionalObjective> =
			find(and(RegionalObjective::world eq world, RegionalObjective::type eq type)).toList()

		fun findAllOfType(type: RegionalObjectiveType): List<RegionalObjective> =
			find(RegionalObjective::type eq type).toList()

		fun setNation(id: Oid<RegionalObjective>, nation: Oid<Nation>?) = trx { sess ->
			updateById(sess, id, setValue(RegionalObjective::nation, nation))
		}

		fun setLastSieged(id: Oid<RegionalObjective>, date: Date) =
			updateById(id, setValue(RegionalObjective::lastSieged, date))

		fun delete(id: Oid<RegionalObjective>) = trx { sess ->
			col.deleteOneById(sess, id)
		}
	}
}
