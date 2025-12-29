package net.horizonsend.ion.common.database.schema.nations

import java.time.DayOfWeek
import java.util.Date
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq

/** Referenced on:
 *  - Nation (for the stations they own) */
data class KothStation(
    override val _id: Oid<KothStation>,
    /** The name of the KOTH */
	var name: String,
    /** The world the KOTH is in */
	var world: String,
    /** The X-coordinate of the center of the KOTH */
	var x: Int,
    /** The Z-coordinate of the center of the KOTH */
	var z: Int,
    /** The hour of the siege days the KOTH activates */
	var kothHour: Int,
    /** The days of the week the KOTH activates */
	var kothDays: Set<DayOfWeek> = setOf(),
	/** The list where scores are kept **/
	var kothPoints: MutableMap<Oid<Nation>, Int>,
	/** The nation currently controlling a KOTH **/
	var nation: Oid<Nation>? = null,
	/** The quarter of the day the station can be sieged in (1-4)**/
	var kothTimeFrame: Int = 1

) : DbObject {

	companion object : OidDbObjectCompanion<KothStation>(KothStation::class, setup = {
		ensureUniqueIndex(KothStation::name)
		ensureUniqueIndex(KothStation::world, KothStation::x, KothStation::z)
	}) {
		fun create(
			name: String,
			world: String,
			x: Int,
			z: Int,
			siegeHour: Int,
			siegeDays: Set<DayOfWeek>,
			kothPoints: MutableMap<Oid<Nation>, Int>
		): Oid<KothStation> {
			val id: Oid<KothStation> =
                objId()
			col.insertOne(KothStation(id, name, world, x, z, siegeHour, siegeDays, kothPoints))
			return id
		}

		fun delete(id: Oid<KothStation>) = trx { sess ->
			KothSiege.col.deleteMany(sess, KothStation::_id eq id)
			col.deleteOneById(sess, id)
		}
	}
}

data class KothSiege(
    override val _id: Oid<KothSiege>,
    /** The station that was sieged */
	val station: Oid<KothStation>,
    /** When it was sieged */
	val time: Date,
	/** Specific score */
	//var kothPoints: MutableMap<Oid<Nation>, Int>
) : DbObject {
	companion object : OidDbObjectCompanion<KothSiege>(KothSiege::class, setup = {
		ensureIndex(KothSiege::station)
	}) {
		fun create(kothId: Oid<KothStation>): Oid<KothSiege> {
			val id: Oid<KothSiege> =
                objId()
			col.insertOne(KothSiege(id, kothId, Date(System.currentTimeMillis())))
			return id
		}
	}
}
