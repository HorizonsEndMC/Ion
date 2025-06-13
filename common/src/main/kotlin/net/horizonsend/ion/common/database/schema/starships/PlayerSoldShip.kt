package net.horizonsend.ion.common.database.schema.starships

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.ensureIndex

class PlayerSoldShip(
	override val _id: Oid<PlayerSoldShip>,
	var owner: SLPlayerId,
	var name: String,
	var description: Set<String>,

	var type: StarshipTypeDB,
	var pilotLoc: DBVec3i,

	var size: Int,

	var blockData: String, // base64 representation of the schematic
) : DbObject {
	companion object : OidDbObjectCompanion<PlayerSoldShip>(
		PlayerSoldShip::class,
		setup = {
			ensureIndex(PlayerSoldShip::owner)
			ensureIndex(PlayerSoldShip::name)
			ensureIndex(PlayerSoldShip::type)
			ensureIndex(PlayerSoldShip::size)
		}
	)
}
