package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage

interface PoweredEnviornment : RecipeEnviornment {
	val powerStorage: PowerStorage
}
