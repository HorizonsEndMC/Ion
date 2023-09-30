package net.horizonsend.ion.common.database.schema.starships

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

data class Blueprint(
	override val _id: Oid<Blueprint>,
	var owner: SLPlayerId,
	var name: String,
	var type: StarshipTypeDB,
	var pilotLoc: DBVec3i,
	var size: Int,
	var blockData: String, // base64 representation of the schematic
	var trustedPlayers: MutableSet<SLPlayerId> = mutableSetOf(),
	var trustedNations: MutableSet<Oid<Nation>> = mutableSetOf()
) : DbObject {
	companion object : OidDbObjectCompanion<Blueprint>(Blueprint::class, setup = {
		ensureIndex(Blueprint::owner)
		ensureIndex(Blueprint::name)
		ensureUniqueIndex(Blueprint::owner, Blueprint::name)
	}) {
		fun delete(id: Oid<Blueprint>) = trx { sess ->
			col.deleteOneById(sess, id)
		}

		fun get(owner: SLPlayerId, name: String): Blueprint? {
			return Blueprint.col.findOne(and(Blueprint::owner eq owner, Blueprint::name eq name))
		}

		fun create(owner: SLPlayerId, name: String, type: StarshipTypeDB, pilotLoc: DBVec3i, size: Int, data: String): Oid<Blueprint> {
			val id = objId<Blueprint>()

			Blueprint.col.insertOne(Blueprint(id, owner, name, type, pilotLoc, size, data))

			return id
		}
	}

	override fun hashCode(): Int {
		return _id.hashCode()
	}
}
