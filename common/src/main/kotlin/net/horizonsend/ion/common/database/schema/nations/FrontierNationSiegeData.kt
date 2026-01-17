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
data class FrontierNationSiegeData(
	override val _id: Oid<FrontierNationSiegeData>,

	val zone: Oid<FrontierTerritory>,
	val attacker: Oid<FrontierNation>,
	val defender: Oid<FrontierNation>,

	val attackerPoints: Int = 0,
	val defenderPoints: Int = 0,

	val declareTime: Date = Date(System.currentTimeMillis()),

	val complete: Boolean = false,

	val availableRewards: MutableMap<String, Int> = mutableMapOf()
) : DbObject {
	companion object : OidDbObjectCompanion<FrontierNationSiegeData>(FrontierNationSiegeData::class, setup = {
		ensureIndex(FrontierNationSiegeData::declareTime)
	}) {
		fun new(zone: Oid<FrontierTerritory>, attacker: Oid<FrontierNation>, defender: Oid<FrontierNation>): Oid<FrontierNationSiegeData> = trx { sess ->
			val id = objId<FrontierNationSiegeData>()
			col.insertOne(sess, FrontierNationSiegeData(id, zone, attacker, defender))

			return@trx id
		}

		fun updatePoints(id: Oid<FrontierNationSiegeData>, defender: Int, attacker: Int) {
			updateById(
				id,
				setValue(FrontierNationSiegeData::defenderPoints, defender),
				setValue(FrontierNationSiegeData::attackerPoints, attacker)
			)
		}

		fun markComplete(id: Oid<FrontierNationSiegeData>) {
			col.updateOneById(id, setValue(FrontierNationSiegeData::complete, true))
		}

		fun findActive(): FindIterable<FrontierNationSiegeData> {
			return col.find(and(
				FrontierNationSiegeData::declareTime gte activeOffset,
				FrontierNationSiegeData::complete eq false
			))
		}

		/**
		 * Any siege declared before this time will be over
		 **/
		val activeOffset: Date get() = Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(180 + 90)) /* Siege leadup duration + Siege duration */
	}
}
