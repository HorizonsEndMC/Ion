package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.and
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import java.util.Date

/**
 * Stores siege data in the case of a restart
 **/
data class SolarSiege(
	override val _id: Oid<SolarSiege>,

	val zone: Oid<SolarSiegeZone>,
	val attacker: Oid<Nation>,

	val attackerPoints: Int = 0,
	val defenderPoints: Int = 0,

	val startTime: Date = Date(System.currentTimeMillis())
) : DbObject {
	companion object : OidDbObjectCompanion<SolarSiege>(SolarSiege::class) {
		fun new(zone: Oid<SolarSiegeZone>, attacker: Oid<Nation>): Oid<SolarSiege> = trx { sess ->
			val id = objId<SolarSiege>()
			col.insertOne(sess, SolarSiege(id, zone, attacker))

			return@trx id
		}

		fun updatePoints(id: Oid<SolarSiege>, defender: Int, attacker: Int) {
			col.updateOneById(id, and(
				setValue(SolarSiege::defenderPoints, defender),
				setValue(SolarSiege::attackerPoints, attacker),
			))
		}
	}
}
