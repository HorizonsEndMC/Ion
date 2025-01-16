package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.MultiblockRecipeEnviornment

interface RecipeProcessingMultiblockEntity<Input: MultiblockRecipeEnviornment> {
	fun buildInput(): Input

	fun getRecipesFor() {

	}
}
