package net.horizonsend.ion.common.database.schema.economy

import com.mongodb.client.FindIterable
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.set
import org.litote.kmongo.setTo

/**
 * Referenced by: eco station
 */
data class CollectedItem(
    override val _id: Oid<CollectedItem>,
    var station: Oid<EcoStation>,
    var itemString: String,
    var minStacks: Int,
    var maxStacks: Int,
    var value: Double,
    var stock: Int = 0,
    var sold: Int = stock
) : DbObject {
	companion object : OidDbObjectCompanion<CollectedItem>(CollectedItem::class, setup = {
		ensureIndex(CollectedItem::station)
		ensureIndex(CollectedItem::itemString)
		ensureUniqueIndex(CollectedItem::station, CollectedItem::itemString)
	}) {
		fun create(station: Oid<EcoStation>, string: String, min: Int, max: Int, value: Double): Oid<CollectedItem> {
			val id = objId<CollectedItem>()
			col.insertOne(CollectedItem(id, station, string, min, max, value))
			return id
		}

		fun delete(id: Oid<CollectedItem>) {
			col.deleteOneById(id)
		}

		fun setValue(id: Oid<CollectedItem>, value: Double) {
			updateById(id, org.litote.kmongo.setValue(CollectedItem::value, value))
		}

		fun setStock(id: Oid<CollectedItem>, stock: Int) {
			updateById(id, org.litote.kmongo.setValue(CollectedItem::stock, stock))
		}

		fun setStackRange(id: Oid<CollectedItem>, min: Int, max: Int) {
			updateById(id, set(CollectedItem::minStacks setTo min, CollectedItem::maxStacks setTo max))
		}

		fun findAllAt(stationId: Oid<EcoStation>): FindIterable<CollectedItem> {
			return col.find(CollectedItem::station eq stationId)
		}
	}
}
