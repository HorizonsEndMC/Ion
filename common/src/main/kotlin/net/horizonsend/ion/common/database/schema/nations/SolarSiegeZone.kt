package net.horizonsend.ion.common.database.schema.nations

import com.mongodb.client.result.UpdateResult
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

data class SolarSiegeZone(
	override val _id: Oid<SolarSiegeZone>,
	val name: String,

	var x: Int,
	var z: Int,
	var world: String,

	val nation: Oid<Nation>? = null,

	val rewards: Set<String> = setOf()
) : DbObject {

	companion object : OidDbObjectCompanion<SolarSiegeZone>(
		SolarSiegeZone::class,
		setup = {
			ensureUniqueIndex(SolarSiegeZone::name)
			ensureIndex(SolarSiegeZone::nation)
		}
	) {
		fun create(
			name: String,
			world: String,
			x: Int,
			z: Int
		): Oid<SolarSiegeZone> {
			val id: Oid<SolarSiegeZone> = objId()

			col.insertOne(SolarSiegeZone(id, name, x, z, world))
			return id
		}

		fun setNation(stationId: Oid<SolarSiegeZone>, nationId: Oid<Nation>): UpdateResult {
			return col.updateOneById(stationId, setValue(SolarSiegeZone::nation, nationId))
		}
	}
}
