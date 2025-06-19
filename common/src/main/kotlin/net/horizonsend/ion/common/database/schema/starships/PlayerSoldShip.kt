package net.horizonsend.ion.common.database.schema.starships

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.DBVec3i
import net.horizonsend.ion.common.utils.text.GsonComponentString
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.util.Date

class PlayerSoldShip(
	override val _id: Oid<PlayerSoldShip>,
	val creationTerritory: Oid<*>,
	val creationTime: Date = Date(System.currentTimeMillis()),

	val owner: SLPlayerId,

	val className: String,
	var name: String,
	var description: List<GsonComponentString>? = null,

	val price: Double,

	var pilotLoc: DBVec3i,
	val size: Int,
	var type: StarshipTypeDB,
	override val blockData: String, // base64 representation of the schematic
) : DbObject, BlueprintLike {
	companion object : OidDbObjectCompanion<PlayerSoldShip>(
		PlayerSoldShip::class,
		setup = {
			ensureIndex(PlayerSoldShip::owner)
			ensureIndex(PlayerSoldShip::name)
			ensureIndex(PlayerSoldShip::type)
			ensureIndex(PlayerSoldShip::size)
		}
	) {
		fun delete(id: Oid<Blueprint>) = trx { sess ->
			col.deleteOneById(sess, id)
		}

		fun get(owner: SLPlayerId, name: String): PlayerSoldShip? {
			return col.findOne(and(PlayerSoldShip::owner eq owner, PlayerSoldShip::name eq name))
		}

		fun create(
			owner: SLPlayerId,
			territory: Oid<*>,
			className: String,
			name: String,
			description: List<GsonComponentString>?,
			price: Double,
			type: StarshipTypeDB,
			pilotLoc: DBVec3i,
			size: Int,
			data: String,
		): Oid<PlayerSoldShip> = trx { sess ->
			val id = objId<PlayerSoldShip>()

			col.insertOne(sess, PlayerSoldShip(
				_id = id,
				creationTerritory = territory,
				owner = owner,
				className = className,
				name = name,
				description = description,
				price = price,
				size = size,
				type = type,
				pilotLoc = pilotLoc,
				blockData = data
			))

			id
		}
	}
}
