package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.database.schema.nations.Nation
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import java.util.Date

data class GasDepot(
	override val _id: Oid<GasDepot>,
	var name: String,
	var world: String,
	var x: Int,
	var z: Int,
	var nation: Oid<Nation>? = null,
	var lastSieged: Date? = null
) : DbObject {
	companion object : OidDbObjectCompanion<GasDepot>(GasDepot::class, setup = {
		ensureUniqueIndex(GasDepot::name)
		ensureUniqueIndex(GasDepot::world, GasDepot::x, GasDepot::z)
	}) {
		fun create(name: String, world: String, x: Int, z: Int): Oid<GasDepot> {
			val id = objId<GasDepot>()
			col.insertOne(GasDepot(id, name, world, x, z))
			return id
		}

		fun setNation(id: Oid<GasDepot>, nation: Oid<Nation>?) =
			updateById(id, org.litote.kmongo.setValue(GasDepot::nation, nation))

		fun setLastSieged(id: Oid<GasDepot>, date: Date) =
			updateById(id, org.litote.kmongo.setValue(GasDepot::lastSieged, date))

		fun delete(id: Oid<GasDepot>) = trx { sess ->
			col.deleteOneById(sess, id)
		}
	}
}
