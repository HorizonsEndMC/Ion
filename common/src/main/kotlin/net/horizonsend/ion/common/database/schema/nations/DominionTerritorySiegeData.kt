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
data class DominionTerritorySiegeData(
    override val _id: Oid<DominionTerritorySiegeData>,

    val zone: Oid<DominionTerritory>,
    val attacker: Oid<Nation>,
    val defender: Oid<Nation>,

    val attackerPoints: Int = 0,
    val defenderPoints: Int = 0,

    val declareTime: Date = Date(System.currentTimeMillis()),

    val complete: Boolean = false,

    val availableRewards: MutableMap<String, Int> = mutableMapOf()
) : DbObject {
	companion object : OidDbObjectCompanion<DominionTerritorySiegeData>(DominionTerritorySiegeData::class, setup = {
		ensureIndex(DominionTerritorySiegeData::declareTime)
	}) {
		fun new(zone: Oid<DominionTerritory>, attacker: Oid<Nation>, defender: Oid<Nation>): Oid<DominionTerritorySiegeData> = trx { sess ->
			val id = objId<DominionTerritorySiegeData>()
			col.insertOne(sess, DominionTerritorySiegeData(id, zone, attacker, defender))

			return@trx id
		}

		fun updatePoints(id: Oid<DominionTerritorySiegeData>, defender: Int, attacker: Int) {
			updateById(
				id,
				setValue(DominionTerritorySiegeData::defenderPoints, defender),
				setValue(DominionTerritorySiegeData::attackerPoints, attacker)
			)
		}

		fun markComplete(id: Oid<DominionTerritorySiegeData>) {
			col.updateOneById(id, setValue(DominionTerritorySiegeData::complete, true))
		}

		fun findActive(): FindIterable<DominionTerritorySiegeData> {
			return col.find(and(
				DominionTerritorySiegeData::declareTime gte activeOffset,
				DominionTerritorySiegeData::complete eq false
			))
		}

		/**
		 * Any siege declared before this time will be over
		 **/
		val activeOffset: Date get() = Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(180 + 90)) /* Siege leadup duration + Siege duration */
	}
}
