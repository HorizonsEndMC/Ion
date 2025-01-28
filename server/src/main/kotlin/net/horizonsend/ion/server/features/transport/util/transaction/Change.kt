package net.horizonsend.ion.server.features.transport.util.transaction

import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.abs

interface Change {
	fun check(inventory: Inventory): Boolean

	fun execute(inventory: Inventory): Boolean

	class ItemRemoval(val item: ItemStack, val amount: Int, val similarityProvider: (ItemStack, ItemStack) -> Boolean) : Change {
		override fun check(inventory: Inventory): Boolean {
			TODO("Not yet implemented")
		}

		override fun execute(inventory: Inventory): Boolean {
			val byCount = mutableMapOf<ItemStack, Int>()
			val contents = inventory.contents.filterNotNull()

			for (item in contents) {
				val asOne = item.asOne()
				if (byCount.containsKey(asOne)) continue
				val count = contents.sumOf { stack -> if (stack.isSimilar(item)) stack.amount else 0 }
				byCount[asOne] = count
			}

			val toRemove = abs(amount)

			if (byCount.getOrDefault(item, 0) <= toRemove) return false

			var remaining = toRemove

			var iterations = 0
			// Upper bound of inventory as double chest size
			while (remaining > 0 && iterations < 54) {
				iterations++
				val item = contents.first { stack -> similarityProvider(stack, item) }
				val amount = item.amount

				val toRemove = minOf(amount, remaining)

				remaining -= toRemove
				item.amount -= toRemove
			}

			return true
		}
	}

	class ItemAddition(val item: ItemStack, val amount: Int) : Change {
		override fun check(inventory: Inventory): Boolean {
			TODO("Not yet implemented")
		}

		override fun execute(inventory: Inventory): Boolean {
			if (!LegacyItemUtils.canFit(inventory, item, amount)) {
				return false
			}

			val stacks = mutableListOf<ItemStack>()

			var remaining = amount
			var iterations = 0
			while (remaining > 0 && iterations < 54) {
				iterations++

				val maxStackSize = item.maxStackSize
				val newAmount = minOf(maxStackSize, remaining)
				remaining -= newAmount
				stacks.add(item.asQuantity(newAmount))
			}

			stacks.forEach(inventory::addItem)

			return true
		}
	}
}
