package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.spacestation.SpaceStationInterface
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.Id
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.path
import org.litote.kmongo.pull
import org.litote.kmongo.setValue
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KMutableProperty1

class StationZone(
	override val _id: Oid<StationZone> = objId(),
	val station: Oid<SpaceStationInterface<*>>,
	var world: String,
	var name: String,
	var minPoint: DBVec3i,
	var maxPoint: DBVec3i,
	var owner: SLPlayerId? = null,
	var price: Int? = null,
	var rent: Int? = null,
	var motd: String? = null,
	var trustedPlayers: Set<SLPlayerId>? = null,
	var trustedNations: Set<Oid<Nation>>? = null,
	var trustedSettlements: Set<Oid<Settlement>>? = null,
	var minBuildAccess: Settlement.ForeignRelation? = null,
	var allowFriendlyFire: Boolean? = null,
	var interactableBlocks: Set<String> = setOf()
) : DbObject {
	companion object : OidDbObjectCompanion<StationZone>(StationZone::class, setup = {
		ensureIndex(StationZone::station)
		ensureUniqueIndexCaseInsensitive(StationZone::name)
	}) {
		fun getByName(station: Oid<SpaceStationInterface<*>>, name: String): Oid<StationZone>? = findOneProp(
			and(StationZone::station eq station, StationZone::name eq name),
			StationZone::_id
		)

		fun create(station: Oid<SpaceStationInterface<*>>, world: String, name: String, p1: DBVec3i, p2: DBVec3i): Oid<StationZone> {
			require(none(StationZone::name eq name)) { "Zone named $name already exists" }

			val minPoint = DBVec3i(min(p1.x, p2.x), min(p1.y, p2.y), min(p1.z, p2.z))
			val maxPoint = DBVec3i(max(p1.x, p2.x), max(p1.y, p2.y), max(p1.z, p2.z))

			val id: Oid<StationZone> =
                objId()

			col.insertOne(StationZone(id, station, world, name, minPoint, maxPoint))

			return id
		}

		fun delete(zone: Oid<StationZone>) {
			col.deleteOneById(zone)
		}

		fun setPrice(zone: Oid<StationZone>, price: Int?) {
			require(price == null || price >= 0) { "$price is an invalid price" }
//            require(!matches(sess, zone, SettlementZone::owner ne null)) { "Zone $zone has an owner" } // zone owner can put it up for sale
			updateById(zone, setValue(StationZone::price, price))
		}

		fun setRent(zone: Oid<StationZone>, rent: Int?): Unit = trx { sess ->
			require(rent == null || rent >= 1) { "$rent is an invalid rent" }
			require(!matches(sess, zone, StationZone::owner ne null)) { "Zone $zone has an owner" }

			updateById(sess, zone, setValue(StationZone::rent, rent))
		}

		fun setOwner(zoneId: Oid<StationZone>, owner: SLPlayerId?) {
			if (owner == null) {
				// un-setting owner
				updateById(
					zoneId,
                    setValue(StationZone::owner, null),
                    setValue(StationZone::price, null),
                    setValue(StationZone::rent, null),
                    setValue(StationZone::motd, null),
                    setValue(StationZone::trustedPlayers, null),
                    setValue(StationZone::trustedNations, null),
                    setValue(StationZone::trustedSettlements, null),
                    setValue(StationZone::minBuildAccess, null),
                    setValue(StationZone::allowFriendlyFire, null),
                    setValue(StationZone::interactableBlocks, setOf())
				)
			} else {
				// someone bought it -> set owner, unset price (not rent, as the rent must stay for them to be charged)
				updateById(
					zoneId,
                    setValue(StationZone::owner, owner),
                    setValue(StationZone::price, null)
				)
			}
		}

		private fun <A, B : Id<A>> addTrusted(
            zoneId: Oid<StationZone>,
            id: B,
            property: KMutableProperty1<StationZone, Set<B>?>
		): Unit = trx { sess ->
			require(matches(sess, zoneId, StationZone::owner ne null)) { "Zone $zoneId has no owner" }
			if (matches(sess, zoneId, property eq null)) {
				updateById(sess, zoneId, setValue(property, setOf(id)))
			} else {
				updateById(sess, zoneId, addToSet(property, id))
			}
		}

		fun addTrustedPlayer(zoneId: Oid<StationZone>, playerId: SLPlayerId) =
			addTrusted(zoneId, playerId, StationZone::trustedPlayers)

		fun addTrustedNation(zoneId: Oid<StationZone>, nationId: Oid<Nation>) =
			addTrusted(zoneId, nationId, StationZone::trustedNations)

		fun addTrustedSettlement(zoneId: Oid<StationZone>, settlementId: Oid<Settlement>) =
			addTrusted(zoneId, settlementId, StationZone::trustedSettlements)

		private fun <A, B : Id<A>> removeTrusted(
            zoneId: Oid<StationZone>,
            id: B,
            property: KMutableProperty1<StationZone, Set<B>?>
		): Unit = trx { sess ->
			require(matches(sess, zoneId, StationZone::owner ne null)) { "Zone $zoneId has no owner" }
			require(!matches(sess, zoneId, property eq null)) { "Zone $zoneId has null for ${property.path()}" }
			updateById(sess, zoneId, pull(property, id))
		}

		fun removeTrustedPlayer(zoneId: Oid<StationZone>, playerId: SLPlayerId) =
			removeTrusted(zoneId, playerId, StationZone::trustedPlayers)

		fun removeTrustedNation(zoneId: Oid<StationZone>, nationId: Oid<Nation>) =
			removeTrusted(zoneId, nationId, StationZone::trustedNations)

		fun removeTrustedSettlement(zoneId: Oid<StationZone>, settlementId: Oid<Settlement>) =
			removeTrusted(zoneId, settlementId, StationZone::trustedSettlements)

		fun setAllowFriendlyFire(zoneId: Oid<StationZone>, value: Boolean) = trx { sess ->
			require(matches(sess, zoneId, StationZone::owner ne null)) { "Zone $zoneId has no owner" }
			updateById(sess, zoneId, setValue(StationZone::allowFriendlyFire, value))
		}

		fun addInteractableBlock(zoneId: Oid<StationZone>, blockName: String) = trx { sess ->
			require(matches(sess, zoneId, StationZone::owner ne null)) { "Zone $zoneId has no owner" }
			updateById(sess, zoneId, addToSet(StationZone::interactableBlocks, blockName))
		}

		fun removeInteractableBlock(zoneId: Oid<StationZone>, blockName: String) = trx { sess ->
			require(matches(sess, zoneId, StationZone::owner ne null)) { "Zone $zoneId has no owner" }
			updateById(sess, zoneId, pull(StationZone::interactableBlocks, blockName))
		}

		fun setMinBuildAccess(zoneId: Oid<StationZone>, level: Settlement.ForeignRelation): Unit = trx { sess ->
			require(matches(sess, zoneId, StationZone::owner ne null)) { "Zone $zoneId has no owner" }
			updateById(sess, zoneId, setValue(StationZone::minBuildAccess, level))
		}
	}
}
