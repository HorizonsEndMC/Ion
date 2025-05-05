package net.horizonsend.ion.server.gui.invui.bazaar

import com.mongodb.client.FindIterable
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.descendingSort

enum class BazaarSort(val displayName: Component) {
	MIN_PRICE(text("Min Price")) {
		private val property = BazaarItem::price

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortBy { it.first.value.maxOfOrNull(property) }
		}

		override fun sort(collection: FindIterable<BazaarItem>) {
			collection.ascendingSort(property)
		}
	},
	MAX_PRICE(text("Max Price")) {
		private val property = BazaarItem::price

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortByDescending { it.first.value.maxOfOrNull(property) }
		}

		override fun sort(collection: FindIterable<BazaarItem>) {
			collection.descendingSort(property)
		}
	},
	HIGHEST_STOCK(text("Highest Stock")) {
		private val property = BazaarItem::stock

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortByDescending { it.first.value.maxOfOrNull(property) }
		}

		override fun sort(collection: FindIterable<BazaarItem>) {
			collection.descendingSort(property)
		}
	},
	LOWEST_STOCK(text("Lowest Stock")) {
		private val property = BazaarItem::stock

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortBy { it.first.value.maxOfOrNull(property) }
		}

		override fun sort(collection: FindIterable<BazaarItem>) {
			collection.ascendingSort(property)
		}
	},
	HIGHEST_BALANCE(text("Highest Balance")) {
		private val property = BazaarItem::balance

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortByDescending { it.first.value.maxOfOrNull(property) }
		}

		override fun sort(collection: FindIterable<BazaarItem>) {
			collection.descendingSort(property)
		}
	},
	LOWEST_BALANCE(text("Lowest Balance")) {
		private val property = BazaarItem::balance

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortBy { it.first.value.maxOfOrNull(property) }
		}

		override fun sort(collection: FindIterable<BazaarItem>) {
			collection.ascendingSort(property)
		}
	},
	HIGHEST_LISTINGS(text("Highest Listings")) {
		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortByDescending { it.first.value.size }
		}

		override fun sort(collection: FindIterable<BazaarItem>) {}
	},
	LOWEST_LISTINGS(text("Lowest Listings")) {
		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortBy { it.first.value.size }
		}

		override fun sort(collection: FindIterable<BazaarItem>) {}
	}

	;

	abstract fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>)
	abstract fun sort(collection: FindIterable<BazaarItem>)
}
