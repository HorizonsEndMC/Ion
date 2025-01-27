package net.horizonsend.ion.server.features.transport.util

import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import org.bukkit.inventory.ItemStack
import kotlin.math.abs

class ItemTransaction(val originCacheHolder: CacheHolder<ItemTransportCache>, val similarityProvider: (ItemStack, ItemStack) -> Boolean) {
	data class ItemDiff(val item: ItemStack, val amount: Int)
	val removes = multimapOf<BlockKey, ItemDiff>()
	val adds = multimapOf<BlockKey, ItemDiff>()

	fun addRemoval(sourcePos: BlockKey, diff: ItemDiff) {
		removes[sourcePos].add(diff)
	}

	fun addAddition(sourcePos: BlockKey, diff: ItemDiff) {
		adds[sourcePos].add(diff)
	}

	/**
	 * Commits the item difference
	 * Returns if the commit was successful
	 **/
	fun commitItemDiff(localKey: BlockKey, diff: ItemDiff): Boolean {
		val inventory = originCacheHolder.cache.getInventory(localKey) ?: return false

		// If remove
		if (diff.amount < 0) {
			val byCount = mutableMapOf<ItemStack, Int>()
			val contents = inventory.contents.filterNotNull()

			for (item in contents) {
				val asOne = item.asOne()
				if (byCount.containsKey(asOne)) continue
				val count = contents.sumOf { stack -> if (stack.isSimilar(item)) stack.amount else 0 }
				byCount[asOne] = count
			}

			val toRemove = abs(diff.amount)

			if (byCount.getOrDefault(diff.item, 0) <= toRemove) return false

			var remaining = toRemove

			var iterations = 0
			// Upper bound of inventory as double chest size
			while (remaining > 0 && iterations < 54) {
				iterations++
				val item = contents.first { stack -> similarityProvider(stack, diff.item) }
				val amount = item.amount

				val toRemove = minOf(amount, remaining)

				remaining -= toRemove
				item.amount -= toRemove
			}

			return true
		}

		// If add
		if (!LegacyItemUtils.canFit(inventory, diff.item, diff.amount)) {
			return false
		}

		val stacks = mutableListOf<ItemStack>()

		var remaining = diff.amount
		var iterations = 0
		while (remaining > 0 && iterations < 54) {
			iterations++

			val maxStackSize = diff.item.maxStackSize
			val newAmount = minOf(maxStackSize, remaining)
			remaining -= newAmount
			stacks.add(diff.item.asQuantity(newAmount))
		}

		stacks.forEach(inventory::addItem)

		return true
	}

	fun commit() {
		for (key in removes.keys()) {
			val diffs = removes[key]
			for (diff in diffs) {
				if (commitItemDiff(key, diff)) return
			}
		}

		for (key in adds.keys()) {
			val diffs = adds[key]
			for (diff in diffs) {
				if (commitItemDiff(key, diff)) return
			}
		}
	}
}
