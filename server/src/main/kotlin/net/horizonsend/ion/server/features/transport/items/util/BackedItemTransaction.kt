package net.horizonsend.ion.server.features.transport.items.util

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap
import net.horizonsend.ion.server.features.transport.nodes.util.PathfindingNodeWrapper
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class BackedItemTransaction(
	val source: ItemReference,
	val item: ItemStack,
	private val amountProvider: Supplier<Int>,
	val destinations: Object2ObjectRBTreeMap<PathfindingNodeWrapper, CraftInventory>,
	val destinationSelector: (Object2ObjectRBTreeMap<PathfindingNodeWrapper, CraftInventory>) -> Pair<PathfindingNodeWrapper, CraftInventory>
) {
	fun execute() {
		val amount = amountProvider.get()
		if (amount <= 0) return

		val cloned = source.inventory.getItem(source.index)?.clone() ?: return
		val stackSize = cloned.amount
		val notRemoved = tryRemove()

		val limit = amount - notRemoved

		if (limit <= 0) return

		val notAdded = addToDestination(limit)
		val newStackSize = stackSize - limit + notAdded
		if (newStackSize <= 0) return

		source.inventory.setItem(source.index, cloned.asQuantity(newStackSize))
	}

	// Returns amount that could not be removed
	private fun tryRemove(): Int {
		val amount = amountProvider.get()
		if (amount <= 0) return 0

		val sourceStack = source.inventory.getItem(source.index) ?: return amount
		val removeAmount = minOf(amount, sourceStack.amount)

		val nmsContainer = source.inventory.inventory
		if (nmsContainer is BlockEntity) {
			nmsContainer.setChanged()
		}

		return if (amount == removeAmount) {
			source.inventory.setItem(source.index, null)

			0
		} else {
			sourceStack.amount -= removeAmount

			amount - removeAmount
		}
	}

	// Returns amount that did not fit
	private fun addToDestination(limit: Int): Int {
		var remaining = limit

		var destinationsRemaining = destinations.size

		while (remaining > 0 && destinationsRemaining >= 1) {
			destinationsRemaining--
			val destination = destinationSelector(destinations)
			val remainder = addToInventory(destination.second, item.asQuantity(remaining))

			if (remainder == 0) return 0
			remaining -= (remaining - remainder)
			destinations.remove(destination.first)
		}

		return remaining
	}
}
