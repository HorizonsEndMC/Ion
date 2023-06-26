package net.horizonsend.ion.server.database.schema.nations.moonsieges

import com.mongodb.client.FindIterable
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.trx
import net.starlegacy.util.Vec3i
import net.starlegacy.util.isInRange
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Sign
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq

data class SiegeBeacon(
	override val _id: Oid<SiegeBeacon>,

	var name: String,

	var siegeTerritory: Oid<SiegeTerritory>,

	var owner: Oid<Nation>?,
	var attacker: Oid<Nation>,

	var points: Int,

	var world: String,
	var x: Int,
	var y: Int,
	var z: Int,

	var blocks: LongArray,
) : DbObject {
//	@Transient
//	var laser: Laser.CrystalLaser? = null

	companion object : OidDbObjectCompanion<SiegeBeacon>(
		SiegeBeacon::class,
		{
			ensureUniqueIndexCaseInsensitive(SiegeBeacon::name)
			ensureIndex(SiegeBeacon::world)
			ensureIndex(SiegeBeacon::siegeTerritory)
		}
	) {
		const val BEACON_DETECTION_RADIUS = 500
		const val BEACON_CAPTURE_RADIUS = 25
		const val BEACON_SIEGE_MAX_TIME_MS: Long = 1000 * 60 * 60 * 2

		fun getBeacons(siegeTerritory: Oid<SiegeTerritory>): FindIterable<SiegeBeacon> =
			col.find(SiegeBeacon::siegeTerritory eq siegeTerritory)

		fun create(name: String, siegeTerritory: Oid<SiegeTerritory>, attacker: Oid<Nation>, world: String, signLoc: Vec3i, blocks: LongArray): Oid<SiegeBeacon> = trx { session ->
			val id = objId<SiegeBeacon>()

			val (x, y, z) = signLoc

			col.insertOne(
				session,
				SiegeBeacon(
					_id = id,
					name = name,
					siegeTerritory = siegeTerritory,
					attacker = attacker,
					world = world,
					x = x,
					y = y,
					z = z,
					blocks = blocks,
					owner = null,
					points = 0
				)
			)

			return@trx id
		}
	}
}
