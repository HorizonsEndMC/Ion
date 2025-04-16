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
		val cloned = source.get()?.clone() ?: return

		val (addedInventories, notAdded) = addToDestination(amount)
		if (notAdded == amount) return

		val notRemoved = tryRemove(amount - notAdded)

		if (notRemoved > 0) {
			var removeAmount = notRemoved

			for ((addedInventory, amount) in addedInventories) {
				if (removeAmount <= 0) break
				if (amount <= 0) continue

				val missing = addedInventory.removeItem(cloned.asQuantity(amount)).entries.firstOrNull()?.key ?: 0
				removeAmount -= (amount - missing)
			}
		}
	}

	// Returns amount that could not be removed
	private fun tryRemove(toRemove: Int): Int {
		val sourceStack = source.inventory.getItem(source.index) ?: return toRemove
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

		return toRemove - removeAmount
	}

	// Returns amount that did not fit
	private fun addToDestination(limit: Int): Pair<Map<CraftInventory, Int>, Int> {
		var remaining = limit

		var destinationsRemaining = destinations.size

		val added = mutableMapOf<CraftInventory, Int>()

		while (remaining > 0 && destinationsRemaining >= 1) {
			destinationsRemaining--
			val destination = destinationSelector(destinations)
			val notAdded = addToInventory(destination.second, item.asQuantity(remaining))
			added[destination.second] = remaining - notAdded

			if (notAdded == 0) {
				return added to 0
			}

			remaining -= (remaining - notAdded)
			destinations.remove(destination.first)
		}

		return added to remaining
	}
}
