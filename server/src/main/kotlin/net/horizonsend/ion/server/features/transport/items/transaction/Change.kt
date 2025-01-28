package net.horizonsend.ion.server.features.transport.items.transaction

import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.abs

interface Change {
	fun check(inventory: Inventory): Boolean

	fun execute(inventory: Inventory): Boolean

	class ItemRemoval(val item: ItemStack, val amount: Int) : Change {
		override fun check(inventory: Inventory): Boolean {
			return true //TODO
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

			if (byCount.getOrDefault(item, 0) < toRemove) return false

			var remaining = toRemove

			var iterations = 0
			// Upper bound of inventory as double chest size
			while (remaining > 0 && iterations < 54) {
				iterations++
				val invItem = contents.first { stack -> stack.isSimilar(item) }
				val amount = invItem.amount

				val toRemove = minOf(amount, remaining)

				remaining -= toRemove

				if (toRemove == invItem.maxStackSize) {
					inventory.remove(invItem)
				}

				invItem.amount -= toRemove
			}

			return true
		}
	}

	class ItemAddition(val item: ItemStack, val amount: Int) : Change {
		override fun check(inventory: Inventory): Boolean {
			return LegacyItemUtils.canFit(inventory, item, amount) //TODO
		}

		override fun execute(inventory: Inventory): Boolean {
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
