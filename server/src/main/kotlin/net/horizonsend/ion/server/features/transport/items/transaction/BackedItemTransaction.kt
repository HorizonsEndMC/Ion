package net.horizonsend.ion.server.features.transport.items.transaction

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class BackedItemTransaction(val source: ItemReference, val item: ItemStack, val amount: Int, val destination: Inventory) {
	fun check(): Boolean {
		return true
	}

	fun execute() {
		val cloned = source.inventory.getItem(source.index)?.clone() ?: return
		val notRemoved = tryRemove()

		println("Not removed: $notRemoved")
		val limit = amount - notRemoved

		println("Limit: $limit")

		if (limit <= 0) return

		println("Adding to destination")
		val notAdded = addToDestination(limit)
		println("Not added: $notAdded")
		if (notAdded <= 0) return

		source.inventory.setItem(source.index, cloned.asQuantity(notAdded))
	}

	// Returns amount that could not be removed
	fun tryRemove(): Int {
		val stack = source.inventory.getItem(source.index)
		if (stack == null) return amount

		val removeAmount = minOf(amount, stack.amount)

		return if (amount == removeAmount) {
			source.inventory.setItem(source.index, null)

			0
		} else {
			source.inventory.getItem(source.index)?.amount -= removeAmount

			amount - removeAmount
		}
	}

	// Returns amount that did not fit
	fun addToDestination(limit: Int): Int {
		return destination.addItem(item.asQuantity(limit)).values.firstOrNull()?.amount ?: 0
	}
}
