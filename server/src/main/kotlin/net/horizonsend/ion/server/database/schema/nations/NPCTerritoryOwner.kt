package net.horizonsend.ion.server.database.schema.nations

import com.mongodb.client.model.Filters
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.trx
import org.bson.conversions.Bson
import org.litote.kmongo.combine
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq

data class NPCTerritoryOwner(
	override val _id: Oid<NPCTerritoryOwner> = objId(),
	val territory: Oid<Territory>,
	val name: String,
	val color: Int,
	val tradeCity: Boolean,
) : DbObject {
	companion object : OidDbObjectCompanion<NPCTerritoryOwner>(NPCTerritoryOwner::class, setup = {
		ensureUniqueIndex(NPCTerritoryOwner::territory)
		ensureUniqueIndex(NPCTerritoryOwner::name)
	}) {
		fun nameQuery(name: String): Bson = Filters.regex("name", "^$name$", "i")

		fun findByName(name: String): Oid<NPCTerritoryOwner>? {
			return findOneProp(nameQuery(name), NPCTerritoryOwner::_id)
		}

		fun getName(id: Oid<NPCTerritoryOwner>): String? = findPropById(id, NPCTerritoryOwner::name)

		fun create(territory: Oid<Territory>, name: String, color: Int, tradeCity: Boolean): Oid<NPCTerritoryOwner> = trx { sess ->
			require(Territory.matches(sess, territory, Territory.unclaimedQuery))

			val id: Oid<NPCTerritoryOwner> =
				objId()

			Territory.updateById(
				sess, territory,
				org.litote.kmongo.setValue(Territory::npcOwner, id),
				org.litote.kmongo.setValue(Territory::isProtected, true)
			)

			col.insertOne(sess, NPCTerritoryOwner(id, territory, name, color, tradeCity))

			return@trx id
		}

		fun delete(id: Oid<NPCTerritoryOwner>): Unit = trx { sess ->
			require(exists(sess, id))
			Territory.col.updateOne(
				sess,
				Territory::npcOwner eq id,
				combine(
					org.litote.kmongo.setValue(Territory::npcOwner, null),
					org.litote.kmongo.setValue(Territory::isProtected, false)
				)
			)
			col.deleteOneById(sess, id)
		}
	}
}
