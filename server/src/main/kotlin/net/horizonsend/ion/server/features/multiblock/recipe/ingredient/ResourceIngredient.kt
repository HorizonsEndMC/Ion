package net.horizonsend.ion.server.features.multiblock.recipe.ingredient

import net.horizonsend.ion.server.features.machine.PowerMachines.getPower
import net.horizonsend.ion.server.features.machine.PowerMachines.removePower
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

class ResourceIngredient(val namespace: NamespacedKey, val amount: Int) : MultiblockRecipeIngredient {
	override fun checkRequirement(multiblock: Multiblock, sign: Sign, input: Inventory): Boolean {
		//TODO expand with gasses during multiblock update
		return getPower(sign, true) >= amount
	}

	override fun consume(multiblock: Multiblock, sign: Sign, input: Inventory) {
		removePower(sign, amount)
		//TODO("Not yet implemented")
	}
}
