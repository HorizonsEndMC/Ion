package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

class ItemIngredient(val ingredient: ItemStack, val amount: Int) : MultiblockRecipeIngredient, ItemConsumable {
	constructor(ingredient: CustomItem, amount: Int) : this(ingredient.constructItemStack(), amount)

	override fun checkRequirement(multiblock: Multiblock, sign: Sign, itemStack: ItemStack?): Boolean {
		if (itemStack == null) return false
		if (!itemStack.isSimilar(ingredient)) return false

		return itemStack.amount >= amount
	}

	override fun consume(multiblock: Multiblock, sign: Sign, itemStack: ItemStack) {
		itemStack.amount -= amount
	}
}
