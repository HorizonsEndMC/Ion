package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue

data class DominionTerritory(
	override val _id: Oid<DominionTerritory> = objId(),
	var name: String,
	var world: String,
	var nation: Oid<Nation>? = null,
	var alias: String? = null
) : DbObject {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		other as DominionTerritory
		if (name != other.name) return false
		return _id == other._id && world == other.world
	}

	override fun hashCode(): Int = _id.hashCode()

	companion object : OidDbObjectCompanion<DominionTerritory>(DominionTerritory::class, setup = {
		ensureUniqueIndex(DominionTerritory::world)   // world name is the natural unique key
		ensureIndex(DominionTerritory::nation)
	}) {
		val unclaimedQuery = DominionTerritory::nation eq null

		fun create(world: String, name: String): Oid<DominionTerritory> = trx { sess ->
			val id = objId<DominionTerritory>()
			col.insertOne(sess, DominionTerritory(
				_id = id,
				name = name,
				world = world
			))
			return@trx id
		}

		fun findByWorld(world: String): DominionTerritory? = trx { sess ->
			col.findOne(sess, DominionTerritory::world eq world)
		}

		fun setNation(id: Oid<DominionTerritory>, nation: Oid<Nation>?): Unit = trx { sess ->
			if (nation != null) {
				require(matches(sess, id, unclaimedQuery))
				require(Nation.exists(sess, nation))
			}

			if (nation == null) {
				updateById(sess, id, setValue(DominionTerritory::alias, null))
			}

			updateById(sess, id, setValue(DominionTerritory::nation, nation))
		}
	}
}
