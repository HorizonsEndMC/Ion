package net.horizonsend.ion.server.gui.invui.bazaar

import com.mongodb.client.FindIterable
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.descendingSort

enum class BazaarSort(val displayName: Component) {
	MIN_PRICE(text("Min Price")) {
		private val property = BazaarItem::price

		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {
			collection.sortBy { it.value.maxOfOrNull(property) }
		}

		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {
			collection.ascendingSort(property)
		}
		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {
			collection.ascendingSort(BazaarOrder::requestedQuantity, BazaarOrder::pricePerItem)
		}
	},
	MAX_PRICE(text("Max Price")) {
		private val property = BazaarItem::price

		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {
			collection.sortByDescending { it.value.maxOfOrNull(property) }
		}

		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {
			collection.descendingSort(property)
		}
		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {
			collection.descendingSort(BazaarOrder::requestedQuantity, BazaarOrder::pricePerItem)
		}
	},
	HIGHEST_STOCK(text("Highest Stock")) {
		private val property = BazaarItem::stock

		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {
			collection.sortByDescending { it.value.maxOfOrNull(property) }
		}

		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {
			collection.descendingSort(property)
		}
		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {
			collection.descendingSort(BazaarOrder::stock)
		}
	},
	LOWEST_STOCK(text("Lowest Stock")) {
		private val property = BazaarItem::stock

		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {
			collection.sortBy { it.value.maxOfOrNull(property) }
		}

		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {
			collection.ascendingSort(property)
		}
		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {
			collection.ascendingSort(BazaarOrder::stock)
		}
	},
	HIGHEST_BALANCE(text("Highest Balance")) {
		private val property = BazaarItem::balance

		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {
			collection.sortByDescending { it.value.maxOfOrNull(property) }
		}

		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {
			collection.descendingSort(property)
		}

		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {}
	},
	LOWEST_BALANCE(text("Lowest Balance")) {
		private val property = BazaarItem::balance

		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {
			collection.sortBy { it.value.maxOfOrNull(property) }
		}

		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {
			collection.ascendingSort(property)
		}

		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {}
	},
	HIGHEST_LISTINGS(text("Highest Listings")) {
		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {
			collection.sortByDescending { it.value.size }
		}

		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {}
		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {}
	},
	LOWEST_LISTINGS(text("Lowest Listings")) {
		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {
			collection.sortBy { it.value.size }
		}

		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {}
		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {}
	},
	ALPHABETICAL(text("Alphabetical")) {
		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {
			collection.sortBy { it.key }
		}

		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {
			collection.ascendingSort(BazaarItem::itemString)
		}

		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {
			collection.ascendingSort(BazaarOrder::itemString)
		}
	},
	HIGHEST_ORDER_SIZE(text("Highest Order Size")) {
		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {}
		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {}

		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {
			collection.descendingSort(BazaarOrder::requestedQuantity)
		}
	},
	LOWEST_ORDER_SIZE(text("Lowest Order Size")) {
		override fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>) {}
		override fun sortSellOrders(collection: FindIterable<BazaarItem>) {}

		override fun sortBuyOrders(collection: FindIterable<BazaarOrder>) {
			collection.ascendingSort(BazaarOrder::requestedQuantity)
		}
	}

	;

	abstract fun sortSellOrders(collection: MutableList<Map.Entry<String, List<BazaarItem>>>)
	abstract fun sortSellOrders(collection: FindIterable<BazaarItem>)
	abstract fun sortBuyOrders(collection: FindIterable<BazaarOrder>)
}
