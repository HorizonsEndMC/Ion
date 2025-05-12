package net.horizonsend.ion.common.database.schema.economy

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.trx
import org.bson.conversions.Bson
import org.litote.kmongo.combine
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.setValue
import java.time.Instant
import java.util.Date

class BazaarOrder(
	override val _id: Oid<BazaarOrder>,
	val player: SLPlayerId,
	val cityTerritory: Oid<Territory>,

	val itemString: String,
	val requestedQuantity: Int,
	val pricePerItem: Double,

	var lastUpdated: Date,

	var fulfilledQuantity: Int,
	var stock: Int,
	var balance: Double,
) : DbObject {
	companion object : OidDbObjectCompanion<BazaarOrder>(BazaarOrder::class, setup = {
		ensureIndex(BazaarOrder::cityTerritory)
		ensureIndex(BazaarOrder::player)
		ensureIndex(BazaarOrder::itemString)
	}) {
		fun create(
			player: SLPlayerId,
			cityTerritory: Oid<Territory>,
			itemString: String,
			quantity: Int,
			pricePerItem: Double
		): Oid<BazaarOrder> = trx { sess ->
			val id = objId<BazaarOrder>()

			val item = BazaarOrder(
				_id = id,
				player = player,
				cityTerritory = cityTerritory,
				itemString = itemString,
				requestedQuantity = quantity,
				pricePerItem = pricePerItem,
				lastUpdated = Date.from(Instant.now()),
				fulfilledQuantity = 0,
				stock = 0,
				balance = pricePerItem * quantity
			)

			col.insertOne(sess, item)

			return@trx id
		}

		private fun alsoUpdateTime(update: Bson): Bson {
			val date = Date.from(Instant.now())
			return combine(update, setValue(BazaarOrder::lastUpdated, date))
		}
	}
}
