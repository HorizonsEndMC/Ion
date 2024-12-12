package net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient

import net.horizonsend.ion.server.features.custom.CustomItem
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

class ConsumedItemIngredient(ingredient: ItemStack, amount: Int) : ItemIngredient(ingredient, amount), ItemConsumable {
	constructor(ingredient: CustomItem, amount: Int) : this(ingredient.constructItemStack(), amount)

	override fun consume(multiblock: Multiblock, sign: Sign, itemStack: ItemStack) {
		itemStack.amount -= amount
	}
}
