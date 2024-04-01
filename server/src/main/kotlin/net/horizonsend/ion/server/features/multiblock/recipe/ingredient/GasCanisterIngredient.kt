package net.horizonsend.ion.server.features.multiblock.recipe.ingredient

import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.customitems.GasCanister
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import kotlin.math.min

class GasCanisterIngredient(val canister: GasCanister, val amount: Int) : MultiblockRecipeIngredient {
	override fun checkRequirement(multiblock: Multiblock, sign: Sign, input: Inventory): Boolean {
		if (!input.containsAtLeast(canister.constructItemStack(), amount)) return false

		val canisters = input.filter { it.customItem?.identifier == canister.identifier }

		if (canisters.isEmpty()) return false

		return canisters.sumOf { canister.getFill(it) } >= amount
	}

	override fun consume(multiblock: Multiblock, sign: Sign, input: Inventory) {
		val items = input.filter { it.customItem?.identifier == canister.identifier }.associateWith { canister.getFill(it) }

		var remaining = amount

		for ((itemStack, fill) in items) {
			val toRemove = min(remaining, fill)

			canister.setFill(itemStack, fill - toRemove)

			remaining -= toRemove

			if (remaining == 0) break
		}
	}
}
