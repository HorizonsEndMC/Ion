package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement

class PowerRequirement(val amount: Int) : RecipeRequirement<Int> {
	override fun ensureAvailable(resource: Int): Boolean {
		return resource >= amount
	}
}
