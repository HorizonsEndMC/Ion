package net.starlegacy.util

import java.lang.Math.min
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object LegacyItemUtils {
	val CONTRABAND = ChatColor.DARK_RED.toString() + "" + ChatColor.MAGIC + ChatColor.BOLD + "Contraband"

	fun makeContraband(item: ItemStack) {
		if (isContraband(item)) {
			return
		}

		val lore: MutableList<String> = item.lore ?: mutableListOf()
		lore.add(CONTRABAND)
		item.lore = lore
	}

	fun isContraband(item: ItemStack?): Boolean {
		if (item == null || !item.hasItemMeta()) {
			return false
		}

		return item.lore?.contains(CONTRABAND) == true
	}

	@JvmOverloads
	fun canFit(inventory: Inventory, item: ItemStack, amount: Int = item.amount): Boolean {
		return getSpaceFor(inventory, item) >= amount
	}

	fun getSpaceFor(inventory: Inventory, item: ItemStack): Int {
		var space = 0
		val maxStackSize = item.maxStackSize.toInt()
		if (inventory is FurnaceInventory) {
			val furnaceInventory = inventory
			val fuel = furnaceInventory.fuel
			if (fuel == null) {
				space += maxStackSize
			} else if (item.isSimilar(fuel)) {
				space += maxStackSize - fuel.amount
			}

			val smelting = furnaceInventory.smelting
			if (smelting == null) {
				space += maxStackSize
			} else if (item.isSimilar(smelting)) {
				space += maxStackSize - smelting.amount
			}
		} else
			for (stack in inventory.storageContents)
				if (stack == null) {
					space += maxStackSize
				} else if (stack.isSimilar(item)) {
					space += maxStackSize - stack.amount
				}
		return space
	}

	fun limitToMaxStackSize(item: ItemStack): List<ItemStack> {
		val maxStackSize = item.maxStackSize
		val itemAmount = item.amount
		if (itemAmount <= maxStackSize) return listOf(item)
		val fullStacks = itemAmount / maxStackSize
		val extraAmount = itemAmount % maxStackSize
		val stacks = fullStacks + min(extraAmount, 1)
		val items = arrayOfNulls<ItemStack>(stacks)
		val fullStack = item.clone()
		fullStack.amount = maxStackSize
		for (i in 0 until fullStacks) {
			items[i] = fullStack.clone()
		}
		if (extraAmount > 0) {
			val extra = item.clone()
			extra.amount = extraAmount
			items[fullStacks] = extra
		}
		return items.filterNotNull()
	}

	fun addToInventory(inventory: Inventory, vararg item: ItemStack): Boolean {
		return inventory.addItem(*item).size == 0
	}

	fun removeInventoryItems(inv: Inventory, type: Material, amount: Int) {
		var currentAmount = amount
		val items = inv.contents
		for (i in items.indices) {
			val `is` = items[i]
			if (`is` != null && `is`.type == type) {
				val newAmount = `is`.amount - currentAmount
				if (newAmount > 0) {
					`is`.amount = newAmount
					break
				} else {
					items[i] = ItemStack(Material.AIR)
					currentAmount = -newAmount
					if (currentAmount == 0)
						break
				}
			}
		}
		inv.contents = items
	}

	fun getTotalItems(inv: Inventory, type: Material): Int {
		var amount = 0
		for (`is` in inv.contents) {
			if (`is` != null && `is`.type == type) {
				amount += `is`.amount
			}
		}
		return amount
	}

	fun getTotalItems(inv: Inventory, item: ItemStack): Int {
		var amount = 0
		for (`is` in inv.contents) {
			if (`is` != null) {
				if (`is`.isSimilar(item))
					amount += `is`.amount
			}
		}
		return amount
	}
}
