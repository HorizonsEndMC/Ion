package net.horizonsend.ion.common.database.schema.economy

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.and
import org.litote.kmongo.inc
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import java.util.concurrent.TimeUnit

data class StationRentedArea(
	override val _id: Oid<StationRentedArea>,
	var name: String,

	val world: String,
	val minPoint: DBVec3i,
	val maxPoint: DBVec3i,

	var rent: Int = 0,

	var owner: SLPlayerId? = null,
	var rentBalance: Double = 0.0,
	var rentLastCharged: Long = 0
) : DbObject {
	companion object : OidDbObjectCompanion<StationRentedArea>(StationRentedArea::class, setup = {

	}) {
		fun create(name: String, world: String, minPoint: DBVec3i, maxPoint: DBVec3i): Oid<StationRentedArea> {
			return  trx { sess ->
				val id = objId<StationRentedArea>()

				col.insertOne(sess, StationRentedArea(
					_id = id,
					name = name,
					world = world,
					minPoint = minPoint,
					maxPoint = maxPoint
				))

				id
			}
		}

		fun isRentDue(id: Oid<StationRentedArea>): Boolean {
			val lastCharged = findPropById(id, StationRentedArea::rentLastCharged) ?: return false
			return TimeUnit.DAYS.toMillis(30) >= (System.currentTimeMillis() - lastCharged)
		}

		fun chargeRent(id: Oid<StationRentedArea>) {

		}

		/** Returns whether using the available balance, rent can be charged */
		fun canPayRent(id: Oid<StationRentedArea>): Boolean {
			TODO()
		}

		/** Sets the owner */
		fun buy(id: Oid<StationRentedArea>, owner: SLPlayerId) {
			col.updateOneById(id, setValue(StationRentedArea::owner, owner))
		}

		/** Deposit rent to the balance to be charged */
		fun depositRent(id: Oid<StationRentedArea>, amount: Int) {
			col.updateOneById(id, inc(StationRentedArea::rentBalance, amount))
		}

		/** Resets the ownership of this area */
		fun repossess(id: Oid<StationRentedArea>) {
			col.updateOneById(id, and(
				setValue(StationRentedArea::owner, null),
				setValue(StationRentedArea::rentBalance, 0.0),
				setValue(StationRentedArea::rentLastCharged, 0L)
			))
		}
	}
}
