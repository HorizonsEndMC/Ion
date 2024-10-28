package net.horizonsend.ion.common.database.schema.nations

import com.mongodb.client.FindIterable
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.and
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Stores siege data in the case of a restart
 **/
data class SolarSiegeData(
	override val _id: Oid<SolarSiegeData>,

	val zone: Oid<SolarSiegeZone>,
	val attacker: Oid<Nation>,

	val attackerPoints: Int = 0,
	val defenderPoints: Int = 0,

	val declareTime: Date = Date(System.currentTimeMillis()),

	val complete: Boolean = false
) : DbObject {
	companion object : OidDbObjectCompanion<SolarSiegeData>(SolarSiegeData::class, setup = {
		ensureIndex(SolarSiegeData::declareTime)
	}) {
		fun new(zone: Oid<SolarSiegeZone>, attacker: Oid<Nation>): Oid<SolarSiegeData> = trx { sess ->
			val id = objId<SolarSiegeData>()
			col.insertOne(sess, SolarSiegeData(id, zone, attacker))

			return@trx id
		}

		fun updatePoints(id: Oid<SolarSiegeData>, defender: Int, attacker: Int) {
			col.updateOneById(id, and(
				setValue(SolarSiegeData::defenderPoints, defender),
				setValue(SolarSiegeData::attackerPoints, attacker),
			))
		}

		fun markComplete(id: Oid<SolarSiegeData>) {
			col.updateOneById(id, setValue(SolarSiegeData::complete, true))
		}

		fun findActive(): FindIterable<SolarSiegeData> {
			return col.find(and(
				SolarSiegeData::declareTime gte activeOffset,
				SolarSiegeData::complete eq false
			))
		}

		/**
		 * Any siege declared before this time will be over
		 **/
		val activeOffset: Date get() = Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(180 + 90)) /* Siege leadup duration + Siege duration */
	}
}
