package net.horizonsend.ion.server.features.transport.items.util

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap
import net.horizonsend.ion.server.features.transport.nodes.util.PathfindingNodeWrapper
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack

class ItemTransaction {
	private val transactions = mutableListOf<BackedItemTransaction>()

	fun addTransfer(
		sourceReference: ItemReference,
		destinationInventories: Object2ObjectRBTreeMap<PathfindingNodeWrapper, CraftInventory>,
		transferredItem: ItemStack,
		transferredAmountProvider: Int,
		destinationSelector: (Object2ObjectRBTreeMap<PathfindingNodeWrapper, CraftInventory>) -> Pair<PathfindingNodeWrapper, CraftInventory>
	) {
		transactions += BackedItemTransaction(sourceReference, transferredItem, transferredAmountProvider, destinationInventories, destinationSelector)
	}

	fun isEmpty() = transactions.isEmpty()

	fun commit() {
		transactions.forEach { t -> t.execute() }
	}
}
