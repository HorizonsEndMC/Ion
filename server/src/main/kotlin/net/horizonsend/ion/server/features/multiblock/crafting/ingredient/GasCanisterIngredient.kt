package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.GasCanister
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

class GasCanisterIngredient(private val canister: GasCanister, val amount: Int) : MultiblockRecipeIngredient, ItemConsumable {
	override fun checkRequirement(multiblock: Multiblock, sign: Sign, itemStack: ItemStack?): Boolean {
		if (itemStack == null) return false
		if (itemStack.customItem != canister) return false

		return canister.getFill(itemStack) >= amount
	}

	override fun consume(multiblock: Multiblock, sign: Sign, itemStack: ItemStack) {
		canister.setFill(itemStack, canister.getFill(itemStack) - amount)
	}
}
