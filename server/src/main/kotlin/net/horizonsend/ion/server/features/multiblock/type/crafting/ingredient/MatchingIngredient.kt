package net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.miscellaneous.utils.getMatchingMaterials
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

open class MatchingIngredient(val match: String, val ignore : Set<Material>, val amount: Int) : MultiblockRecipeIngredient {
	val matches = run {
		val list: MutableSet<Material> = getMatchingMaterials { it.name.matches(Regex(match)) }
		list.removeAll(ignore)
		list.toSet()
	}

	override fun checkRequirement(multiblock: Multiblock, sign: Sign, itemStack: ItemStack?): Boolean {
		if (itemStack == null) return false
		matches.any()

		if (matches.any{!itemStack.isSimilar(ItemStack(it))}) return false

		return itemStack.amount >= amount
	}
}
