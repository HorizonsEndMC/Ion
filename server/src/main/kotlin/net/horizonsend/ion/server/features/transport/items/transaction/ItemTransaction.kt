package net.horizonsend.ion.server.features.transport.items.transaction

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ItemTransaction() {
	val transactions = mutableListOf<BackedItemTransaction>()

	fun addTransfer(sourceReference: ItemReference, destinationInventory: Inventory, transferredItem: ItemStack, transferredAmount: Int) {
		transactions += BackedItemTransaction(sourceReference, transferredItem, transferredAmount, destinationInventory)
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
