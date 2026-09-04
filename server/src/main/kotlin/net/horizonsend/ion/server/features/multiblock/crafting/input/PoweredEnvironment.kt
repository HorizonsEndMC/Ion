package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage

interface PoweredEnvironment : RecipeEnvironment {
	val powerStorage: PowerStorage
}
