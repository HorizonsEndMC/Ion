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
import org.litote.kmongo.pull
import org.litote.kmongo.push
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

class StationRentalArea(
	override val _id: Oid<StationRentalArea>,

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
	companion object : OidDbObjectCompanion<StationRentalArea>(StationRentalArea::class, setup = {
		ensureUniqueIndex(StationRentalArea::name, StationRentalArea::station, indexOptions = IndexOptions().textVersion(3))
		ensureIndex(StationRentalArea::owner)
		ensureIndex(StationRentalArea::station)
		ensureIndex(StationRentalArea::world)
	}) {
		fun create(name: String, parent: Oid<NPCSpaceStation>, world: String, signLocation: DBVec3i, minPoint: DBVec3i, maxPoint: DBVec3i, rent: Double): Oid<StationRentalArea> = trx { sess ->
			val id = objId<StationRentalArea>()
			col.insertOne(sess, StationRentalArea(
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

		/**  */
		fun depositMoney(id: Oid<StationRentalArea>, amount: Double) {
			col.updateOneById(id, inc(StationRentalArea::rentBalance, amount))
		}

		/** Resets the ownership of this area */
		fun removeOwner(id: Oid<StationRentalArea>) {
			col.updateOneById(id, combine(
				setValue(StationRentalArea::owner, null),
				setValue(StationRentalArea::rentBalance, 0.0),
				setValue(StationRentalArea::rentLastCharged, 0L),
				setValue(StationRentalArea::trustedPlayers, setOf()),
				setValue(StationRentalArea::trustedSettlements, setOf()),
				setValue(StationRentalArea::trustedNations, setOf()),
				setValue(StationRentalArea::collectRentFromOwnerBalance, false),
			))
		}

		fun trustPlayer(id: Oid<StationRentalArea>, trustedId: SLPlayerId) {
			col.updateOneById(id, push(StationRentalArea::trustedPlayers, trustedId))
		}
		fun trustSettlement(id: Oid<StationRentalArea>, trustedId: Oid<Settlement>) {
			col.updateOneById(id, push(StationRentalArea::trustedSettlements, trustedId))
		}
		fun trustNation(id: Oid<StationRentalArea>, trustedId: Oid<Nation>) {
			col.updateOneById(id, push(StationRentalArea::trustedNations, trustedId))
		}

		fun unTrustPlayer(id: Oid<StationRentalArea>, trustedId: SLPlayerId) {
			col.updateOneById(id, pull(StationRentalArea::trustedPlayers, trustedId))
		}
		fun unTrustSettlement(id: Oid<StationRentalArea>, trustedId: Oid<Settlement>) {
			col.updateOneById(id, pull(StationRentalArea::trustedSettlements, trustedId))
		}
		fun unTrustNation(id: Oid<StationRentalArea>, trustedId: Oid<Nation>) {
			col.updateOneById(id, pull(StationRentalArea::trustedNations, trustedId))
		}
	}
}
