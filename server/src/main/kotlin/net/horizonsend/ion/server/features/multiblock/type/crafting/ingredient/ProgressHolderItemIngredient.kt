package net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient

import net.horizonsend.ion.server.features.custom.CustomItemRegistry.newCustomItem
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.misc.ProgressHolder
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

class ProgressHolderItemIngredient(val initialIngredient: ItemIngredient, val progressHolderResult: NewCustomItem) : MultiblockRecipeIngredient {
	override fun checkRequirement(multiblock: Multiblock, sign: Sign, itemStack: ItemStack?): Boolean {
		if (initialIngredient.checkRequirement(multiblock, sign, itemStack)) return true

		if (itemStack == null) return false
		if (itemStack.newCustomItem !is ProgressHolder) return false

		return ProgressHolder.getResult(itemStack) == progressHolderResult
	}
}
