package net.horizonsend.ion.server.features.transport.items.util

import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack

class BackedItemTransaction(
	val source: ItemReference,
	val item: ItemStack,
	private val amount: Int,
	val destinations: MutableCollection<PathfindResult>,
	val destinationSelector: () -> Pair<PathfindResult, CraftInventory>
) {
	fun execute(): Set<CraftInventory> {
		if (amount <= 0) return setOf()
		val cloned = source.get()?.clone() ?: return setOf()

		val (addedInventories, notAdded) = addToDestination(amount)
		if (notAdded == amount) return setOf()

		val notRemoved = tryRemove(amount - notAdded)

		if (notRemoved > 0) {
			var removeAmount = notRemoved

			for ((addedInventory, amount) in addedInventories) {
				if (removeAmount <= 0) break
				if (amount <= 0) continue

				val missing = addedInventory.removeItem(cloned.asQuantity(amount)).entries.firstOrNull()?.key ?: 0
				removeAmount -= (amount - missing)

				if (missing == amount) return setOf()
			}
		}

		return addedInventories.keys
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
			val (pathfindResult, destinationInv) = destinationSelector()
			val notAdded = addToInventory(destinationInv, item.asQuantity(remaining))
			added[destinationInv] = remaining - notAdded

			if (notAdded == 0) {
				return added to 0
			}

			remaining -= (remaining - notAdded)
			destinations.remove(pathfindResult)
		}

		return added to remaining
	}
}
