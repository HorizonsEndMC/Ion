package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.gui.item.AsyncItem

enum class BazaarMergedSort {
	MAX_PRICE {
		private val property = BazaarItem::price

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortByDescending { it.first.value.maxOfOrNull(property) }
		}
	},
	MIN_PRICE {
		private val property = BazaarItem::price

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortBy { it.first.value.maxOfOrNull(property) }
		}
	},
	HIGHEST_STOCK {
		private val property = BazaarItem::stock

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortByDescending { it.first.value.maxOfOrNull(property) }
		}
	},
	LOWEST_STOCK {
		private val property = BazaarItem::stock

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortBy { it.first.value.maxOfOrNull(property) }
		}
	}

	;

	abstract fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>)
}
