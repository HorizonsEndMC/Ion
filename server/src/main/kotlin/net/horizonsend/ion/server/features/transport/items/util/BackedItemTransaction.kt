package net.horizonsend.ion.server.features.transport.items.util

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap
import net.horizonsend.ion.server.features.transport.nodes.util.PathfindingNodeWrapper
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack

class BackedItemTransaction(
	val source: ItemReference,
	val item: ItemStack,
	private val amount: Int,
	val destinations: Object2ObjectRBTreeMap<PathfindingNodeWrapper, CraftInventory>,
	val destinationSelector: (Object2ObjectRBTreeMap<PathfindingNodeWrapper, CraftInventory>) -> Pair<PathfindingNodeWrapper, CraftInventory>
) {
	fun execute() {
		if (amount <= 0) return

		val notAdded = addToDestination(amount)
		if (notAdded == amount) return

		tryRemove(amount - notAdded)
	}

	// Returns amount that could not be removed
	private fun tryRemove(toRemove: Int) {
		val sourceStack = source.inventory.getItem(source.index) ?: return
		val removeAmount = minOf(toRemove, sourceStack.amount)

		if (sourceStack.amount == removeAmount) {
			source.inventory.setItem(source.index, null)
		} else {
			sourceStack.amount -= removeAmount
		}

		val nmsContainer = source.inventory.inventory
		if (nmsContainer is BlockEntity) {
			nmsContainer.setChanged()
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
