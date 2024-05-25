package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

open class ItemIngredient(val ingredient: ItemStack, val amount: Int) : MultiblockRecipeIngredient {
	constructor(ingredient: CustomItem, amount: Int) : this(ingredient.constructItemStack(), amount)

	override fun checkRequirement(multiblock: Multiblock, sign: Sign, itemStack: ItemStack?): Boolean {
		if (itemStack == null) return false
		if (!itemStack.isSimilar(ingredient)) return false

		return itemStack.amount >= amount
	}
}
