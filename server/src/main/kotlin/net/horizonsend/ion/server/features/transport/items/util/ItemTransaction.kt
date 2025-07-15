package net.horizonsend.ion.server.features.transport.items.util

import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack

class ItemTransaction {
	private val transactions = mutableListOf<BackedItemTransaction>()

	fun addTransfer(
		sourceReference: ItemReference,
		destinationInventories: MutableCollection<PathfindResult>,
		transferredItem: ItemStack,
		transferredAmountProvider: Int,
		destinationSelector: () -> Pair<PathfindResult, CraftInventory>
	) {
		transactions += BackedItemTransaction(sourceReference, transferredItem, transferredAmountProvider, destinationInventories, destinationSelector)
	}

	fun isEmpty() = transactions.isEmpty()

	fun commit() {
		transactions.forEach { t -> t.execute() }
	}
}
