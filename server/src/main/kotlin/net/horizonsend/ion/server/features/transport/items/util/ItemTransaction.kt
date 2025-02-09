package net.horizonsend.ion.server.features.transport.items.util

import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack

class ItemTransaction {
	private val transactions = mutableListOf<BackedItemTransaction>()

	fun addTransfer(sourceReference: ItemReference, destinationInventory: CraftInventory, transferredItem: ItemStack, transferredAmount: Int) {
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
