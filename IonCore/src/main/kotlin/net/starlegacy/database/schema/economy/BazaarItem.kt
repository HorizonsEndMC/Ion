package net.starlegacy.database.schema.economy

import java.time.Instant
import java.util.Date
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.objId
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.database.trx
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

data class BazaarItem(
	override val _id: Oid<BazaarItem>,
	val cityTerritory: Oid<Territory>,
	val seller: SLPlayerId,
	var itemString: String,
	var price: Double,
	var stock: Int,
	var lastUpdated: Date,
	var balance: Double
) : DbObject {
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
			cityTerritory: Oid<Territory>, seller: SLPlayerId, itemString: String, price: Double
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

		fun addStock(itemId: Oid<BazaarItem>, amount: Int): Unit = trx { sess ->
			require(amount >= 0)
			col.updateOneById(sess, itemId, alsoUpdateTime(inc(BazaarItem::stock, amount)))
		}

		fun hasStock(itemId: Oid<BazaarItem>, amount: Int): Boolean = matches(itemId, BazaarItem::stock gte amount)

		fun removeStock(itemId: Oid<BazaarItem>, amount: Int): Unit = trx { sess ->
			require(amount >= 0)
			require(matches(sess, itemId, BazaarItem::stock gte amount)) {
				"Bazaar item $itemId does not have enough in stock to remove $amount"
			}
			col.updateOneById(sess, itemId, alsoUpdateTime(inc(BazaarItem::stock, -amount)))
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
			val total = col.find(sess, BazaarItem::seller eq seller).sumByDouble { it.balance }
			col.updateMany(sess, BazaarItem::seller eq seller, org.litote.kmongo.setValue(BazaarItem::balance, 0.0))
			return@trx total
		}
	}
}
