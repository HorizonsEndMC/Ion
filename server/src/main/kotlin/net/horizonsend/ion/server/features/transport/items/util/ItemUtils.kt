package net.horizonsend.ion.server.features.transport.items.util

import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.type.GasFuel
import net.horizonsend.ion.server.features.gas.type.GasOxidizer
import net.horizonsend.ion.server.features.machine.GeneratorFuel
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.minecraft.world.inventory.AbstractFurnaceMenu
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.craftbukkit.inventory.CraftInventoryFurnace
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

fun getTransferSpaceFor(inventory: Collection<CraftInventory>, itemStack: ItemStack): Int = inventory.sumOf {
	getTransferSpaceFor(it, itemStack)
}

fun getTransferSpaceFor(inventory: CraftInventory, itemStack: ItemStack): Int {
	val nmsContainer = inventory.inventory
	val maxStackSize = itemStack.maxStackSize

	if (nmsContainer is AbstractFurnaceBlockEntity) {
		var space = 0

		val fuel = inventory.contents[AbstractFurnaceMenu.FUEL_SLOT]
		if (fuel == null) {
			space += maxStackSize
		} else if (itemStack.isSimilar(fuel)) {
			space += maxStackSize - fuel.amount
		}

		val smelting = inventory.contents[AbstractFurnaceMenu.INGREDIENT_SLOT]
		if (smelting == null) {
			space += maxStackSize
		} else if (itemStack.isSimilar(smelting)) {
			space += maxStackSize - smelting.amount
		}

		return space
	}

	return LegacyItemUtils.getSpaceFor(inventory, itemStack)
}

fun getRemovableItems(inventory: CraftInventory): Iterable<Pair<Int, ItemStack>> {
	val items = inventory.contents.withIndex()
	val filtered = mutableListOf<Pair<Int, ItemStack>>()

	when (inventory.inventory) {
		is AbstractFurnaceBlockEntity -> {
			for ((index, value) in items) {
				if (value == null) continue
				if (index == AbstractFurnaceMenu.RESULT_SLOT) continue

				filtered.add(index to value)
			}
		}

		else -> {
			for ((index, value) in items) {
				if (value == null) continue

				filtered.add(index to value)
			}
		}
	}

	return filtered
}

/**
 * This method will move the item to the appropriate slot of an inventory.
 */
fun addToInventory(inventory: CraftInventory, itemStack: ItemStack): Int {
	val nmsContainer = inventory.inventory

	if (nmsContainer is AbstractFurnaceBlockEntity) {
		return addToFurnace(CraftInventoryFurnace(nmsContainer), itemStack)
	}

	return inventory.addItem(itemStack).entries.firstOrNull()?.value?.amount ?: 0
}

fun addToFurnace(destination: FurnaceInventory, itemStack: ItemStack): Int {
	val toSlot = when {
		destination.smelting?.type == Material.PRISMARINE_CRYSTALS -> 1 // Smelting has crystals, put it in fuel
		destination.fuel?.type == Material.PRISMARINE_CRYSTALS -> 0 // Fuel has crystals, put it in smelting
		itemStack.type.isFuel || GeneratorFuel.getFuel(itemStack) != null -> 1 // slot 1 - fuel
		(itemStack.customItem as? GasCanister)?.gas is GasFuel -> 0 // slot 0 - smelting
		(itemStack.customItem as? GasCanister)?.gas is GasOxidizer -> 1 // slot 1- fuel
		else -> 0 // slot 0 - smelting
	}

	val current: ItemStack? = destination.getItem(toSlot)

	val itemAmount: Int = itemStack.amount

	when {
		// if it's a free space, just put the item in
		current == null || current.type == Material.AIR -> {
			destination.setItem(toSlot, itemStack)
			return 0
		}
		// if it's similar attempt to merge
		current.isSimilar(itemStack) -> {
			val maxAmount: Int = current.maxStackSize
			val freeSpace: Int = maxAmount - current.amount
			when {
				// there is no space, just don't move the item
				freeSpace == 0 -> return itemAmount
				// if the free space is less than the item amount, move as much as possible
				freeSpace < itemAmount -> {
					current.amount = maxAmount

					return itemAmount - freeSpace
				}
				// there's space to move the whole item if the free space is >= the item's amount
				freeSpace >= itemAmount -> {
					current.amount += itemAmount
					return 0
				}
				// this shouldn't even be possible?
				else -> error("Unsupported furnace movement! Free space: $freeSpace, item amount: $itemAmount")
			}
		}
		// if it is not the same type of item, it cannot stack, do not attempt to move
		else -> return itemAmount
	}
}

/**
 * Gets the slots that can be added to in an inventory
 * TODO use this
 */
fun getAdditionSlots(inventory: CraftInventory): Array<Int> {
	val nmsContainer = inventory.inventory

	if (nmsContainer is AbstractFurnaceBlockEntity) {
		return arrayOf(AbstractFurnaceMenu.INGREDIENT_SLOT, AbstractFurnaceMenu.FUEL_SLOT)
	}

	var index = -1
	return Array(inventory.size) { index++ }
}
