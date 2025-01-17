package net.horizonsend.ion.server.features.multiblock.crafting.util

import com.google.common.base.Supplier
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

/**
 * Handles the addition of items to a slot in an inventory
 **/
class SlotModificationWrapper(private val getter: Supplier<ItemStack?>, private val setter: Consumer<ItemStack>) {
	fun removeFromSlot(amount: Int) {
		getter.get()?.amount -= amount
	}

	fun addToSlot(newItem: ItemStack) {
		val currentItem = getter.get()

		if (currentItem == null || currentItem.isEmpty) {
			setter.accept(newItem)
			return
		}

		currentItem.amount = (currentItem.amount + newItem.amount).coerceIn(1, newItem.maxStackSize)
	}

	fun verifySpace(newItem: ItemStack): Boolean {
		val resultOccupant = getter.get() ?: return true
		if (resultOccupant.isEmpty) return true
		if (!resultOccupant.isSimilar(newItem)) return false

		val maxStackSize = resultOccupant.maxStackSize
		return resultOccupant.amount + newItem.amount <= maxStackSize
	}

	companion object {
		fun furnaceSmelting(inventory: FurnaceInventory): SlotModificationWrapper = SlotModificationWrapper(inventory::getSmelting, inventory::setSmelting)
		fun furnaceFuel(inventory: FurnaceInventory): SlotModificationWrapper = SlotModificationWrapper(inventory::getFuel, inventory::setFuel)
		fun furnaceResult(inventory: FurnaceInventory): SlotModificationWrapper = SlotModificationWrapper(inventory::getResult, inventory::setResult)
	}
}
