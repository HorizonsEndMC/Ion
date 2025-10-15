package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.crafting.util.SlotModificationWrapper
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface SingleItemResultEnviornment : RecipeEnviornment, ItemResultEnviornment {
	override fun addItem(item: ItemStack) {
		getResultItemSlotModifier().addToSlot(item)
	}

	override fun getResultSpaceFor(item: ItemStack): Int {
		val resultOccupant = getResultItem() ?: ItemStack(Material.AIR)
		if (resultOccupant.isEmpty) return item.maxStackSize

		if (!resultOccupant.isSimilar(item)) return 0

		val maxStackSize = item.maxStackSize
		return maxOf(0, maxStackSize - resultOccupant.amount)
	}

	fun getResultItem(): ItemStack?
	fun getResultItemSlotModifier(): SlotModificationWrapper
}
