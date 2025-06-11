package net.horizonsend.ion.common.database.schema.nations.spacestation

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.economy.StationRentalArea
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq

class NPCSpaceStation(
	override val _id: Oid<NPCSpaceStation>,

	var name: String,
	var world: String,
	var x: Int,
	var z: Int,
	var radius: Int,

	var isProtected: Boolean,
	var color: Int = Integer.parseInt("FFFFFF", 16),
	var dynmapDescription: String =""
) : DbObject {

	companion object : OidDbObjectCompanion<NPCSpaceStation>(
		NPCSpaceStation::class,
		setup = {
			ensureUniqueIndex(NPCSpaceStation::name)
		}
	) {
		fun create(name: String, world: String, x: Int, z: Int, radius: Int, isProtected: Boolean): Oid<NPCSpaceStation> = trx { session ->
			require(none(
				and(
				NationSpaceStation::name eq name,
				SettlementSpaceStation::name eq name,
				PlayerSpaceStation::name eq name
			)
			))

			val id = objId<NPCSpaceStation>()
			val station = NPCSpaceStation(
				id,
				name = name,
				world = world,
				x = x,
				z = z,
				radius = radius,
				isProtected = isProtected
			)

			col.insertOne(session, station)
			return@trx id
		}

		fun delete(id: Oid<NPCSpaceStation>) = trx { sess ->
			StationRentalArea.col.deleteMany(StationRentalArea::station eq id)

			col.deleteOneById(sess, id)
		}
	}
}
