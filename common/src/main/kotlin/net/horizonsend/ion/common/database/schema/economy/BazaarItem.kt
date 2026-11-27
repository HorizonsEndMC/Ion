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
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.inc
import org.litote.kmongo.updateOneById
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import java.time.Instant
import java.util.Date

data class BazaarItem(
    override val _id: Oid<BazaarItem>,
    override val cityTerritory: Oid<Territory>,
    val seller: SLPlayerId,
    override var itemString: String,
    var price: Double,
    var stock: Int,
    var lastUpdated: Date,
    var balance: Double
) : BazaarEntry {
	companion object : OidDbObjectCompanion<BazaarItem>(BazaarItem::class, setup = {
		ensureIndex(BazaarItem::cityTerritory)
		ensureIndex(BazaarItem::seller)
		ensureIndex(BazaarItem::itemString)

		// don't allow one person to sell the same type of item for different prices in one city
		ensureUniqueIndex(BazaarItem::cityTerritory, BazaarItem::seller, BazaarItem::itemString)
	}) {
		fun matchQuery(terr: Oid<Territory>, seller: SLPlayerId, itemString: String) = and(
			BazaarItem::cityTerritory eq terr,
			BazaarItem::seller eq seller,
			BazaarItem::itemString eq itemString
		)

		fun create(
            cityTerritory: Oid<Territory>,
            seller: SLPlayerId,
            itemString: String,
            price: Double
		): Oid<BazaarItem> = trx { sess ->
			require(none(sess, matchQuery(cityTerritory, seller, itemString))) {
				"$seller is already selling $itemString at $cityTerritory"
			}

			val id = objId<BazaarItem>()
			val item = BazaarItem(id, cityTerritory, seller, itemString, price, 0, Date.from(Instant.now()), 0.0)
			col.insertOne(sess, item)
			return@trx id
		}

		private fun alsoUpdateTime(update: Bson): Bson {
			val date = Date.from(Instant.now())
			return combine(update, org.litote.kmongo.setValue(BazaarItem::lastUpdated, date))
		}

		fun addStock(itemId: Oid<BazaarItem>, amount: Int): Boolean = trx { sess ->
			require(amount >= 0)
			col.updateOneById(sess, itemId, alsoUpdateTime(inc(BazaarItem::stock, amount))).matchedCount == 1L
		}

		fun hasStock(itemId: Oid<BazaarItem>, amount: Int): Boolean = matches(itemId, BazaarItem::stock gte amount)

		fun removeStock(itemId: Oid<BazaarItem>, amount: Int): Boolean = trx { sess ->
			require(amount >= 0)
			col.updateOne(
				sess,
				Filters.and(idFilterQuery(itemId), BazaarItem::stock gte amount),
				alsoUpdateTime(inc(BazaarItem::stock, -amount))
			).matchedCount == 1L
		}

		fun purchase(itemId: Oid<BazaarItem>, amount: Int, price: Double, sellerCredit: Double): Boolean = trx { sess ->
			require(amount >= 0)
			require(sellerCredit >= 0.0)
			col.updateOne(
				sess,
				Filters.and(
					idFilterQuery(itemId),
					BazaarItem::stock gte amount,
					BazaarItem::price eq price
				),
				alsoUpdateTime(combine(
					inc(BazaarItem::stock, -amount),
					inc(BazaarItem::balance, sellerCredit)
				))
			).matchedCount == 1L
		}

		fun reversePurchase(itemId: Oid<BazaarItem>, amount: Int, sellerCredit: Double): Boolean = trx { sess ->
			require(amount >= 0)
			require(sellerCredit >= 0.0)
			col.updateOneById(
				sess,
				itemId,
				alsoUpdateTime(combine(
					inc(BazaarItem::stock, amount),
					inc(BazaarItem::balance, -sellerCredit)
				))
			).matchedCount == 1L
		}

		fun deleteIfEmpty(itemId: Oid<BazaarItem>): Boolean = trx { sess ->
			col.deleteOne(sess, Filters.and(idFilterQuery(itemId), Filters.eq("stock", 0))).deletedCount == 1L
		}

		fun delete(itemId: Oid<BazaarItem>) = trx { sess ->
			col.deleteOneById(sess, itemId)
		}

		fun setPrice(itemId: Oid<BazaarItem>, newPrice: Double): Unit = trx { sess ->
			require(newPrice > 0)
			col.updateOneById(sess, itemId, org.litote.kmongo.setValue(BazaarItem::price, newPrice))
		}

		fun depositMoney(itemId: Oid<BazaarItem>, amount: Double): Unit = trx { sess ->
			require(amount >= 0)
			col.updateOneById(sess, itemId, inc(BazaarItem::balance, amount))
		}

		fun withdrawMoney(itemId: Oid<BazaarItem>, amount: Int): Unit = trx { sess ->
			require(amount >= 0) { "Amount must be >= 0" }
			require(matches(sess, itemId, BazaarItem::balance gte amount.toDouble())) {
				"Balance must be >= amount"
			}
			col.updateOneById(sess, itemId, inc(BazaarItem::balance, -amount))
		}

		fun collectMoney(seller: SLPlayerId): Double = trx { sess ->
			val listingIds = col.find(sess, BazaarItem::seller eq seller).map { it._id }.toList()
			listingIds.sumOf { listingId ->
				col.findOneAndUpdate(
					sess,
					Filters.and(idFilterQuery(listingId), BazaarItem::seller eq seller),
					org.litote.kmongo.setValue(BazaarItem::balance, 0.0),
					FindOneAndUpdateOptions().returnDocument(ReturnDocument.BEFORE)
				)?.balance ?: 0.0
			}
		}
	}
}
