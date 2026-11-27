package net.horizonsend.ion.common.database.schema.economy

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.trx
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.combine
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.gte
import org.litote.kmongo.inc
import org.litote.kmongo.push
import org.litote.kmongo.setValue
import org.litote.kmongo.eq
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import java.time.Instant
import java.util.Date

class BazaarOrder(
	override val _id: Oid<BazaarOrder>,
	val player: SLPlayerId,
	override val cityTerritory: Oid<Territory>,

	override val itemString: String,

	val requestedQuantity: Int,
	val pricePerItem: Double,
	var balance: Double,

	var lastUpdated: Date,

	var fulfilledQuantity: Int = 0,
	var stock: Int = 0,
	var fulfillments: List<Pair<SLPlayerId, Int>> = listOf()
) : BazaarEntry {
	companion object : OidDbObjectCompanion<BazaarOrder>(BazaarOrder::class, setup = {
		ensureIndex(BazaarOrder::cityTerritory)
		ensureIndex(BazaarOrder::player)
		ensureIndex(BazaarOrder::itemString)
	}) {
		val BazaarOrder.potentialProfit get() = pricePerItem * (requestedQuantity - fulfilledQuantity)

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
				balance = pricePerItem * quantity
			)

			col.insertOne(sess, item)

			return@trx id
		}

		fun delete(orderId: Oid<BazaarOrder>) = trx { session ->
			col.deleteOneById(session, orderId)
		}

		fun deleteIfEmpty(orderId: Oid<BazaarOrder>): BazaarOrder? = trx { sess ->
			col.findOneAndDelete(sess, Filters.and(idFilterQuery(orderId), Filters.eq("stock", 0)))
		}

		private fun alsoUpdateTime(update: Bson): Bson {
			val date = Date.from(Instant.now())
			return combine(update, setValue(BazaarOrder::lastUpdated, date))
		}

		/** Returns whether this order has been fully fulfilled */
		fun isFulfilled(order: Oid<BazaarOrder>): Boolean {
			val requested = findPropById(order, BazaarOrder::requestedQuantity)
			return matches(order, BazaarOrder::fulfilledQuantity gte requested)
		}

		/**
		 * Fulfills the amount of stock to the order
		 * Returns the profit
		 **/
		fun fulfillStock(orderId: Oid<BazaarOrder>, fulfiller: SLPlayerId, amount: Int, pricePerItem: Double): Double? = trx { sess ->
			require(amount >= 0)

			val filter = Filters.and(
				idFilterQuery(orderId),
				BazaarOrder::pricePerItem eq pricePerItem,
				Filters.expr(Document("\$lte", listOf(
					Document("\$add", listOf("\$fulfilledQuantity", amount)),
					"\$requestedQuantity"
				)))
			)
			val profit = amount * pricePerItem
			if (col.findOneAndUpdate(
				sess,
				filter,
				alsoUpdateTime(combine(
					inc(BazaarOrder::fulfilledQuantity, amount),
					inc(BazaarOrder::stock, amount),
					inc(BazaarOrder::balance, -profit),
					push(BazaarOrder::fulfillments, fulfiller to amount)
				)),
				FindOneAndUpdateOptions().returnDocument(ReturnDocument.BEFORE)
			) == null) return@trx null

			profit
		}

		/**
		 * Fulfills the amount of stock to the order
		 **/
		fun collectItemBalance(orderId: Oid<BazaarOrder>, amount: Int): Boolean = trx { sess ->
			require(amount >= 0)
			col.updateOne(
				sess,
				Filters.and(idFilterQuery(orderId), BazaarOrder::stock gte amount),
				alsoUpdateTime(inc(BazaarOrder::stock, -amount))
			).matchedCount == 1L
		}
	}
}
