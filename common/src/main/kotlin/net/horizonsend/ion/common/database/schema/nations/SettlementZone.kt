package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
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
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KMutableProperty1

class SettlementZone(
    override val _id: Oid<SettlementZone> = objId(),
    val settlement: Oid<Settlement>,
    val territory: Oid<Territory>,
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
	companion object : OidDbObjectCompanion<SettlementZone>(SettlementZone::class, setup = {
		ensureIndex(SettlementZone::settlement)
		ensureUniqueIndexCaseInsensitive(SettlementZone::name)
	}) {
		fun getByName(settlement: Oid<Settlement>, name: String): Oid<SettlementZone>? = findOneProp(
			and(SettlementZone::settlement eq settlement, SettlementZone::name eq name),
			SettlementZone::_id
		)

		/**
		 * Create a settlement zone with no owner, buy price, or rent,
		 * owned by the settlement and with the specified name, with the
		 * bounds being defined by the space between the two specified points.
		 *
		 * @param settlement The ID of the settlement to use as the parent. Must exist.
		 * @param name The name to give the settlement zone.
		 *             Must be unique per settlement, lowercase, alphanumeric,
		 *             and between 3 and 50 characters.darcul
		 * @param p1 First position of the bounds
		 * @param p2 Second position of the bounds
		 */
		fun create(settlement: Oid<Settlement>, name: String, p1: DBVec3i, p2: DBVec3i): Oid<SettlementZone> {
			val territory: Oid<Territory> = Settlement.findPropById(settlement, Settlement::territory)
				?: error("Settlement $settlement's territory not found! (Either settlement or its territory is missing)")

			// the name check is complicated for these: they can have underscores. so, do it only in the command.
			/*require(name.isAlphanumeric() && name.length >= 3 && name.length < 50)
			{ "Name $name does not meet criteria" }*/

			require(none(SettlementZone::name eq name)) { "Zone named $name already exists" }

			val minPoint = DBVec3i(min(p1.x, p2.x), min(p1.y, p2.y), min(p1.z, p2.z))
			val maxPoint = DBVec3i(max(p1.x, p2.x), max(p1.y, p2.y), max(p1.z, p2.z))

			val id: Oid<SettlementZone> =
                objId()

			col.insertOne(SettlementZone(id, settlement, territory, name, minPoint, maxPoint))

			return id
		}

		fun delete(zone: Oid<SettlementZone>) {
			col.deleteOneById(zone)
		}

		fun setPrice(zone: Oid<SettlementZone>, price: Int?) {
			require(price == null || price >= 0) { "$price is an invalid price" }
//            require(!matches(sess, zone, SettlementZone::owner ne null)) { "Zone $zone has an owner" } // zone owner can put it up for sale
			updateById(zone, org.litote.kmongo.setValue(SettlementZone::price, price))
		}

		fun setRent(zone: Oid<SettlementZone>, rent: Int?): Unit = trx { sess ->
			require(rent == null || rent >= 1) { "$rent is an invalid rent" }
			require(!matches(sess, zone, SettlementZone::owner ne null)) { "Zone $zone has an owner" }

			updateById(sess, zone, org.litote.kmongo.setValue(SettlementZone::rent, rent))
		}

		fun setOwner(zoneId: Oid<SettlementZone>, owner: SLPlayerId?) {
			if (owner == null) {
				// un-setting owner
				updateById(
					zoneId,
					org.litote.kmongo.setValue(SettlementZone::owner, null),
					org.litote.kmongo.setValue(SettlementZone::price, null),
					org.litote.kmongo.setValue(SettlementZone::rent, null),
					org.litote.kmongo.setValue(SettlementZone::motd, null),
					org.litote.kmongo.setValue(SettlementZone::trustedPlayers, null),
					org.litote.kmongo.setValue(SettlementZone::trustedNations, null),
					org.litote.kmongo.setValue(SettlementZone::trustedSettlements, null),
					org.litote.kmongo.setValue(SettlementZone::minBuildAccess, null),
					org.litote.kmongo.setValue(SettlementZone::allowFriendlyFire, null),
					org.litote.kmongo.setValue(SettlementZone::interactableBlocks, setOf())
				)
			} else {
				// someone bought it -> set owner, unset price (not rent, as the rent must stay for them to be charged)
				updateById(
					zoneId,
					org.litote.kmongo.setValue(SettlementZone::owner, owner),
					org.litote.kmongo.setValue(SettlementZone::price, null)
				)
			}
		}

		private fun <A, B : Id<A>> addTrusted(
            zoneId: Oid<SettlementZone>,
            id: B,
            property: KMutableProperty1<SettlementZone, Set<B>?>
		): Unit = trx { sess ->
			require(matches(sess, zoneId, SettlementZone::owner ne null)) { "Zone $zoneId has no owner" }
			if (matches(sess, zoneId, property eq null)) {
				updateById(sess, zoneId, org.litote.kmongo.setValue(property, setOf(id)))
			} else {
				updateById(sess, zoneId, addToSet(property, id))
			}
		}

		fun addTrustedPlayer(zoneId: Oid<SettlementZone>, playerId: SLPlayerId) =
			addTrusted(zoneId, playerId, SettlementZone::trustedPlayers)

		fun addTrustedNation(zoneId: Oid<SettlementZone>, nationId: Oid<Nation>) =
			addTrusted(zoneId, nationId, SettlementZone::trustedNations)

		fun addTrustedSettlement(zoneId: Oid<SettlementZone>, settlementId: Oid<Settlement>) =
			addTrusted(zoneId, settlementId, SettlementZone::trustedSettlements)

		private fun <A, B : Id<A>> removeTrusted(
            zoneId: Oid<SettlementZone>,
            id: B,
            property: KMutableProperty1<SettlementZone, Set<B>?>
		): Unit = trx { sess ->
			require(matches(sess, zoneId, SettlementZone::owner ne null)) { "Zone $zoneId has no owner" }
			require(!matches(sess, zoneId, property eq null)) { "Zone $zoneId has null for ${property.path()}" }
			updateById(sess, zoneId, pull(property, id))
		}

		fun removeTrustedPlayer(zoneId: Oid<SettlementZone>, playerId: SLPlayerId) =
			removeTrusted(zoneId, playerId, SettlementZone::trustedPlayers)

		fun removeTrustedNation(zoneId: Oid<SettlementZone>, nationId: Oid<Nation>) =
			removeTrusted(zoneId, nationId, SettlementZone::trustedNations)

		fun removeTrustedSettlement(zoneId: Oid<SettlementZone>, settlementId: Oid<Settlement>) =
			removeTrusted(zoneId, settlementId, SettlementZone::trustedSettlements)

		fun setAllowFriendlyFire(zoneId: Oid<SettlementZone>, value: Boolean) = trx { sess ->
			require(matches(sess, zoneId, SettlementZone::owner ne null)) { "Zone $zoneId has no owner" }
			updateById(sess, zoneId, org.litote.kmongo.setValue(SettlementZone::allowFriendlyFire, value))
		}

		fun addInteractableBlock(zoneId: Oid<SettlementZone>, blockName: String) = trx { sess ->
			require(matches(sess, zoneId, SettlementZone::owner ne null)) { "Zone $zoneId has no owner" }
			updateById(sess, zoneId, addToSet(SettlementZone::interactableBlocks, blockName))
		}

		fun removeInteractableBlock(zoneId: Oid<SettlementZone>, blockName: String) = trx { sess ->
			require(matches(sess, zoneId, SettlementZone::owner ne null)) { "Zone $zoneId has no owner" }
			updateById(sess, zoneId, pull(SettlementZone::interactableBlocks, blockName))
		}

		fun setMinBuildAccess(zoneId: Oid<SettlementZone>, level: Settlement.ForeignRelation): Unit = trx { sess ->
			require(matches(sess, zoneId, SettlementZone::owner ne null)) { "Zone $zoneId has no owner" }
			updateById(sess, zoneId, org.litote.kmongo.setValue(SettlementZone::minBuildAccess, level))
		}
	}
}
