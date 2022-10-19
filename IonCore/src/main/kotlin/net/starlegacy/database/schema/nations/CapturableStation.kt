package net.starlegacy.database.schema.nations

import com.mongodb.client.result.UpdateResult
import java.time.DayOfWeek
import java.util.Date
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.objId
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.updateOneById

/** Referenced on:
 *  - Nation (for the stations they own) */
data class CapturableStation(
	override val _id: Oid<CapturableStation>,
	/** The name of the capturable station */
	var name: String,
	/** The world the capturable station is in */
	var world: String,
	/** The X-coordinate of the center of the station */
	var x: Int,
	/** The Z-coordinate of the center of the station */
	var z: Int,
	/** The hour of the siege days the station can be sieged on */
	var siegeHour: Int,
	/** The days of the week the station can be sieged on */
	var siegeDays: Set<DayOfWeek> = setOf(),
	/** The quarter of the day the station can be sieged in (1-4)**/
	var siegeTimeFrame: Int = 1,
	/** The nation that currently owns the station */
	var nation: Oid<Nation>? = null
) : DbObject {

	companion object : OidDbObjectCompanion<CapturableStation>(CapturableStation::class, setup = {
		ensureUniqueIndex(CapturableStation::name)
		ensureIndex(CapturableStation::nation)
		ensureUniqueIndex(CapturableStation::world, CapturableStation::x, CapturableStation::z)
	}) {
		fun create(
			name: String, world: String, x: Int, z: Int, siegeHour: Int, siegeDays: Set<DayOfWeek>
		): Oid<CapturableStation> {
			val id: Oid<CapturableStation> = objId()
			col.insertOne(CapturableStation(id, name, world, x, z, siegeHour, siegeDays))
			return id
		}

		fun setNation(stationId: Oid<CapturableStation>, nationId: Oid<Nation>): UpdateResult {
			return col.updateOneById(stationId, org.litote.kmongo.setValue(CapturableStation::nation, nationId))
		}
	}
}

data class CapturableStationSiege(
	override val _id: Oid<CapturableStationSiege>,
	/** The station that was sieged */
	val station: Oid<CapturableStation>,
	/** When it was sieged */
	val time: Date,
	/** Which nation sieged it */
	val nation: Oid<Nation>
) : DbObject {
	companion object : OidDbObjectCompanion<CapturableStationSiege>(CapturableStationSiege::class, setup = {
		ensureIndex(CapturableStationSiege::station)
		ensureIndex(CapturableStationSiege::nation)
	}) {
		fun create(stationId: Oid<CapturableStation>, nation: Oid<Nation>): Oid<CapturableStationSiege> {
			val id: Oid<CapturableStationSiege> = objId()
			col.insertOne(CapturableStationSiege(id, stationId, Date(System.currentTimeMillis()), nation))
			return id
		}
	}
}
