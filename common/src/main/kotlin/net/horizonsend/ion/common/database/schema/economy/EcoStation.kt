package net.horizonsend.ion.common.database.schema.economy

import com.mongodb.client.model.IndexOptions
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.push

/**
 * Referenced by: Collected item
 */
data class EcoStation(
    override val _id: Oid<EcoStation>,
    var name: String,
    var world: String,
    var x: Int,
    var z: Int,
    var collectors: List<Collector> = listOf(),
    var collectedItems: Set<Oid<CollectedItem>> = setOf()
) : DbObject {
	companion object : OidDbObjectCompanion<EcoStation>(EcoStation::class, setup = {
		ensureUniqueIndexCaseInsensitive(EcoStation::name, indexOptions = IndexOptions().textVersion(3))
		ensureIndex(EcoStation::world)
	}) {
		fun create(name: String, world: String, x: Int, z: Int): Oid<EcoStation> {
			val id = objId<EcoStation>()
			col.insertOne(EcoStation(id, name, world, x, z))
			return id
		}

		fun delete(id: Oid<EcoStation>): Unit = trx { sess ->
			CollectedItem.col.deleteMany(sess, CollectedItem::station eq id)
			col.deleteOneById(sess, id)
		}

		fun addCollector(station: Oid<EcoStation>, x: Int, y: Int, z: Int) {
			updateById(station, push(EcoStation::collectors, Collector(x, y, z)))
		}

		fun clearCollectors(station: Oid<EcoStation>) {
			updateById(station, org.litote.kmongo.setValue(EcoStation::collectors, listOf()))
		}

		fun setCenter(station: Oid<EcoStation>, x: Int, z: Int) {
			updateById(
				station,
				org.litote.kmongo.setValue(EcoStation::x, x),
				org.litote.kmongo.setValue(EcoStation::z, z)
			)
		}

		fun setWorld(station: Oid<EcoStation>, world: String) {
			updateById(station, org.litote.kmongo.setValue(EcoStation::world, world))
		}
	}

	data class Collector(var x: Int, var y: Int, var z: Int)

	fun distance(x: Number, y: Number, z: Number): Double {
		//TODO: Eco stations aren't used
		return /* distance(this.x.toDouble(), 128.0, this.z.toDouble(), x.toDouble(), y.toDouble(), z.toDouble()) */ 0.0
	}
}
