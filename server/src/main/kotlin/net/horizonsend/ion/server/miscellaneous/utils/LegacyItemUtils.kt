package net.horizonsend.ion.server.miscellaneous.utils

import org.bukkit.ChatColor
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object LegacyItemUtils {
	val CONTRABAND = ChatColor.DARK_RED.toString() + "" + ChatColor.MAGIC + ChatColor.BOLD + "Contraband"

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
		} else {
			for (stack in inventory.storageContents)
				if (stack == null) {
					space += maxStackSize
				} else if (stack.isSimilar(item)) {
					space += maxStackSize - stack.amount
				}
		}
		return space
	}

	fun addToInventory(inventory: Inventory, vararg item: ItemStack): Boolean {
		return inventory.addItem(*item).size == 0
	}

	fun getTotalItems(inv: Inventory, item: ItemStack): Int {
		var amount = 0
		for (`is` in inv.contents) {
			if (`is` != null) {
				if (`is`.isSimilar(item)) {
					amount += `is`.amount
				}
			}
		}
		return amount
	}
}
