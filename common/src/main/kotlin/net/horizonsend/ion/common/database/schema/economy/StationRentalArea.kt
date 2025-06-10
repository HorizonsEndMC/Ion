package net.horizonsend.ion.common.database.schema.economy

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.result.DeleteResult
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.combine
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

class StationRentalArea(
	override val _id: Oid<StationRentalArea>,

	val world: String,
	val minPoint: DBVec3i,
	val maxPoint: DBVec3i,

	val name: String,

	var rent: Int,

	var owner: SLPlayerId? = null,
	var rentBalance: Double = 0.0,
	var rentLastCharged: Long = 0
) : DbObject {
	companion object : OidDbObjectCompanion<StationRentalArea>(StationRentalArea::class, setup = {
		ensureUniqueIndexCaseInsensitive(StationRentalArea::name, indexOptions = IndexOptions().textVersion(3))
		ensureIndex(StationRentalArea::world)
	}) {
		fun create(name: String, world: String, minPoint: DBVec3i, maxPoint: DBVec3i, rent: Int): Oid<StationRentalArea> = trx { sess ->
			val id = objId<StationRentalArea>()
			col.insertOne(sess, StationRentalArea(id, world, minPoint, maxPoint, name, rent))
			return@trx id
		}

		fun delete(id: Oid<StationRentalArea>): DeleteResult = trx { sess ->
			col.deleteOneById(sess, id)
		}

		/** Sets the player as owner */
		fun claim(id: Oid<StationRentalArea>, newOwner: SLPlayerId) {
			col.updateOneById(id, combine(
				setValue(StationRentalArea::owner, newOwner),
				setValue(StationRentalArea::rentLastCharged, System.currentTimeMillis())
			))
		}

		/** When an owners are transferring ownership, don't adjust charged rent or anything */
		fun transferOwnership(id: Oid<StationRentalArea>, newOwner: SLPlayerId) {
			col.updateOneById(id, setValue(StationRentalArea::owner, newOwner))
		}

		/** Resets the ownership of this area */
		fun removeOwner(id: Oid<StationRentalArea>) {
			col.updateOneById(id, combine(
				setValue(StationRentalArea::owner, null),
				setValue(StationRentalArea::rentBalance, 0.0),
				setValue(StationRentalArea::rentLastCharged, 0L)
			))
		}
	}
}
