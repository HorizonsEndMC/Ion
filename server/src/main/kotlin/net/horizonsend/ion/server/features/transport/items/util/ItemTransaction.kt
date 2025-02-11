package net.horizonsend.ion.server.features.transport.items.util

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack

class ItemTransaction {
	private val transactions = mutableListOf<BackedItemTransaction>()

	fun addTransfer(
		sourceReference: ItemReference,
		destinationInventories: Long2ObjectRBTreeMap<CraftInventory>,
		transferredItem: ItemStack,
		transferredAmount: Int,
		destinationSelector: (Long2ObjectRBTreeMap<CraftInventory>) -> Pair<BlockKey, CraftInventory>
	) {
		transactions += BackedItemTransaction(sourceReference, transferredItem, transferredAmount, destinationInventories, destinationSelector)
	}

	fun commit() {
		transactions
			.filter { transaction -> transaction.check() }
			.forEach { t -> t.execute() }
	}

	fun checkAll(): Boolean {
		return transactions.all { transaction -> transaction.check() }
	}
}
