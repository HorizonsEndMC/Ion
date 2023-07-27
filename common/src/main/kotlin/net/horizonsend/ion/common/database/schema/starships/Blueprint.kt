package net.horizonsend.ion.common.database.schema.starships

import net.horizonsend.ion.common.database.*
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex

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
	}

	override fun hashCode(): Int {
		return _id.hashCode()
	}
}
