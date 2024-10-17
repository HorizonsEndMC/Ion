package net.horizonsend.ion.server.features.multiblock.crafting.recipe

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import org.bukkit.inventory.Inventory

class RecipeExecutionContext<T: MultiblockEntity>(val recipe: MultiblockRecipe<T>, val entity: T) {
	private var aborted: Boolean = false
	private lateinit var inventories: Map<String, Inventory>

	fun getLabeledInventory(name: String): Inventory = inventories[name]!!

	fun execute() {
		inventories = recipe.getLabeledInventories(this)
		if (aborted) return
		if (!recipe.checkResourcesAvailable(this)) return
		if (aborted) return
		if (!recipe.consumeResources(this)) return
		if (aborted) return
		recipe.finalize(this)
	}

	fun abort() {
		aborted = true
	}
}
