package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import org.bukkit.Material

class CenterBlockRequirement(val predicate: (Material?) -> Boolean): RecipeRequirement<Material?> {
	override fun ensureAvailable(resource: Material?): Boolean {
		return predicate(resource)
	}
}
