package net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient

import net.horizonsend.ion.server.features.custom.CustomItem
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.misc.ProgressHolder
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

class ProgressHolderItemIngredient(val initialIngredient: ItemIngredient, val progressHolderResult: CustomItem) : MultiblockRecipeIngredient {
	override fun checkRequirement(multiblock: Multiblock, sign: Sign, itemStack: ItemStack?): Boolean {
		if (initialIngredient.checkRequirement(multiblock, sign, itemStack)) return true

		if (itemStack == null) return false
		if (itemStack.customItem !is ProgressHolder) return false

		return ProgressHolder.getResult(itemStack) == progressHolderResult
	}
}
