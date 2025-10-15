package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.kyori.adventure.text.Component

interface StatusRecipeEnvironment : RecipeEnviornment {
	fun setStatus(status: Component) {
		(multiblock as StatusMultiblockEntity).setStatus(status)
	}

	fun clearStatus() {
		(multiblock as StatusMultiblockEntity).clearStatus()
	}
}
