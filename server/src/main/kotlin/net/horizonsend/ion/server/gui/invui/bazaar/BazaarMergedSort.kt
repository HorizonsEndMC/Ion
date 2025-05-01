package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

enum class BazaarMergedSort(val displayName: Component) {
	MIN_PRICE(text("Min Price")) {
		private val property = BazaarItem::price

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortBy { it.first.value.maxOfOrNull(property) }
		}
	},
	MAX_PRICE(text("Max Price")) {
		private val property = BazaarItem::price

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortByDescending { it.first.value.maxOfOrNull(property) }
		}
	},
	HIGHEST_STOCK(text("Highest Stock")) {
		private val property = BazaarItem::stock

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortByDescending { it.first.value.maxOfOrNull(property) }
		}
	},
	LOWEST_STOCK(text("Lowest Stock")) {
		private val property = BazaarItem::stock

		override fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>) {
			collection.sortBy { it.first.value.maxOfOrNull(property) }
		}
	}

	;

	abstract fun sort(collection: MutableList<Pair<Map. Entry<String, List<BazaarItem>>, AsyncItem>>)
}
