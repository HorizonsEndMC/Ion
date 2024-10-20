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

data class RentedArea(
	override val _id: Oid<RentedArea>,
	var name: String,

	val world: String,
	val minPoint: DBVec3i,
	val maxPoint: DBVec3i,

	var rent: Int = 0,

	var owner: SLPlayerId? = null,
	var rentBalance: Double = 0.0,
	var rentLastCharged: Long = 0
) : DbObject {
	companion object : OidDbObjectCompanion<RentedArea>(RentedArea::class, setup = {

	}) {
		fun create(name: String, world: String, minPoint: DBVec3i, maxPoint: DBVec3i): Oid<RentedArea> {
			return  trx { sess ->
				val id = objId<RentedArea>()

				col.insertOne(sess, RentedArea(
					_id = id,
					name = name,
					world = world,
					minPoint = minPoint,
					maxPoint = maxPoint
				))

				id
			}
		}

		fun isRentDue(id: Oid<RentedArea>): Boolean {
			val lastCharged = findPropById(id, RentedArea::rentLastCharged) ?: return false
			return TimeUnit.DAYS.toMillis(30) >= (System.currentTimeMillis() - lastCharged)
		}

		fun chargeRent(id: Oid<RentedArea>) {

		}

		/** Returns whether using the available balance, rent can be charged */
		fun canPayRent(id: Oid<RentedArea>): Boolean {
			TODO()
		}

		/** Sets the owner */
		fun buy(id: Oid<RentedArea>, owner: SLPlayerId) {
			col.updateOneById(id, setValue(RentedArea::owner, owner))
		}

		/** Deposit rent to the balance to be charged */
		fun depositRent(id: Oid<RentedArea>, amount: Int) {
			col.updateOneById(id, inc(RentedArea::rentBalance, amount))
		}

		/** Resets the ownership of this area */
		fun repossess(id: Oid<RentedArea>) {
			col.updateOneById(id, and(
				setValue(RentedArea::owner, null),
				setValue(RentedArea::rentBalance, 0.0),
				setValue(RentedArea::rentLastCharged, 0L)
			))
		}
	}
}
