package net.horizonsend.ion.server.database.schema.nations

import com.mongodb.client.model.Filters
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.trx
import org.jetbrains.exposed.sql.transactions.transaction
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex

data class SpaceStation(
	override val _id: Oid<SpaceStation>,
	var nation: Oid<Nation>,
	var name: String,
	var world: String,
	var x: Int,
	var z: Int,
	var radius: Int,
	var managers: Set<SLPlayerId>,
	var trustedPlayers: Set<SLPlayerId>,
	var trustedNations: Set<Oid<Nation>>,
	var trustLevel: TrustLevel
) : DbObject {
	enum class TrustLevel { MANUAL, NATION, ALLY }

	companion object : OidDbObjectCompanion<SpaceStation>(SpaceStation::class, setup = {
		ensureUniqueIndex(SpaceStation::name)
		ensureIndex(SpaceStation::nation)
		ensureIndex(SpaceStation::managers)
		ensureIndex(SpaceStation::trustedPlayers)
		ensureIndex(SpaceStation::trustedNations)
	}) {
		private fun nameQuery(name: String) = Filters.regex("name", "^$name$", "i")

		fun create(
			nation: Oid<Nation>,
			name: String,
			world: String,
			x: Int,
			z: Int,
			radius: Int
		): Oid<SpaceStation> = trx { sess ->
			require(Nation.none(sess, nameQuery(name)))
			val id = objId<SpaceStation>()
			val trustLevel = TrustLevel.MANUAL
			val station = SpaceStation(id, nation, name, world, x, z, radius, setOf(), setOf(), setOf(), trustLevel)
			col.insertOne(sess, station)
			return@trx id
		}

		fun delete(id: Oid<SpaceStation>) {
			col.deleteOneById(id)
		}
	}
}
