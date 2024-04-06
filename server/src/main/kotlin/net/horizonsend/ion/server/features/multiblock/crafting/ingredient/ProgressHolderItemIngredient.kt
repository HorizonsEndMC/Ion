package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.customitems.misc.ProgressHolder
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

class ProgressHolderItemIngredient(val from: ItemIngredient, val ingredient: CustomItem) : MultiblockRecipeIngredient {
	override fun checkRequirement(multiblock: Multiblock, sign: Sign, itemStack: ItemStack?): Boolean {
		if (from.checkRequirement(multiblock, sign, itemStack)) return true

		if (itemStack == null) return false
		if (itemStack.customItem !is ProgressHolder) return false

		return ProgressHolder.getResult(itemStack) == ingredient
	}
}
