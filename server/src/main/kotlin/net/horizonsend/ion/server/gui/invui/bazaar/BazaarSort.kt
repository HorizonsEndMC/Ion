package net.horizonsend.ion.server.gui.invui.bazaar

import com.mongodb.client.FindIterable
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.descendingSort

enum class BazaarSort {
	PRICE {
		private val property = BazaarItem::price

		override fun sort(collection: FindIterable<BazaarItem>, ascending: Boolean) {
			if (ascending) collection.ascendingSort(property) else collection.descendingSort(property)
		}
	}

	;

	abstract fun sort(collection: FindIterable<BazaarItem>, ascending: Boolean)
}
