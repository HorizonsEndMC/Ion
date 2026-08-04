package net.horizonsend.ion.common.database.schema.economy

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.result.DeleteResult
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.spacestation.NPCSpaceStation
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.combine
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.inc
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

class StationRentalZone(
	override val _id: Oid<StationRentalZone>,

	val station: Oid<NPCSpaceStation>,
	val name: String,

	val world: String,
	val signLocation: DBVec3i,
	val minPoint: DBVec3i,
	val maxPoint: DBVec3i,

	var rent: Double,

	var owner: SLPlayerId? = null,
	var trustedPlayers: Set<SLPlayerId> = setOf(),
	var trustedSettlements: Set<Oid<Settlement>> = setOf(),
	var trustedNations: Set<Oid<Nation>> = setOf(),
	var collectRentFromOwnerBalance: Boolean = false,

	var rentBalance: Double = 0.0,
	var rentLastCharged: Long = 0
) : DbObject {
	companion object : OidDbObjectCompanion<StationRentalZone>(StationRentalZone::class, setup = {
		ensureUniqueIndex(StationRentalZone::name, StationRentalZone::station, indexOptions = IndexOptions().textVersion(3))
		ensureIndex(StationRentalZone::owner)
		ensureIndex(StationRentalZone::station)
		ensureIndex(StationRentalZone::world)
	}) {
		fun create(name: String, parent: Oid<NPCSpaceStation>, world: String, signLocation: DBVec3i, minPoint: DBVec3i, maxPoint: DBVec3i, rent: Double): Oid<StationRentalZone> = trx { sess ->
			val id = objId<StationRentalZone>()
			col.insertOne(sess, StationRentalZone(
				_id = id,
				station = parent,
				world = world,
				signLocation = signLocation,
				minPoint = minPoint,
				maxPoint = maxPoint,
				name = name,
				rent = rent
			))
			return@trx id
		}

		fun delete(id: Oid<StationRentalZone>): DeleteResult = trx { sess ->
			col.deleteOneById(sess, id)
		}

		/** Sets the player as owner */
		fun claim(id: Oid<StationRentalZone>, newOwner: SLPlayerId) {
			col.updateOneById(id, combine(
				setValue(StationRentalZone::owner, newOwner),
				setValue(StationRentalZone::rentLastCharged, System.currentTimeMillis())
			))
		}

		/** When an owners are transferring ownership, don't adjust charged rent or anything */
		fun transferOwnership(id: Oid<StationRentalZone>, newOwner: SLPlayerId) {
			col.updateOneById(id, combine(
				setValue(StationRentalZone::owner, newOwner), // Prevent one player from draining another's balance
				setValue(StationRentalZone::collectRentFromOwnerBalance, false)
			))
		}

		/**  */
		fun depositMoney(id: Oid<StationRentalZone>, amount: Double) {
			col.updateOneById(id, inc(StationRentalZone::rentBalance, amount))
		}

		/** Resets the ownership of this zone */
		fun removeOwner(id: Oid<StationRentalZone>) {
			col.updateOneById(id, combine(
				setValue(StationRentalZone::owner, null),
				setValue(StationRentalZone::rentBalance, 0.0),
				setValue(StationRentalZone::rentLastCharged, 0L),
				setValue(StationRentalZone::trustedPlayers, setOf()),
				setValue(StationRentalZone::trustedSettlements, setOf()),
				setValue(StationRentalZone::trustedNations, setOf()),
				setValue(StationRentalZone::collectRentFromOwnerBalance, false),
			))
		}
	}
}
