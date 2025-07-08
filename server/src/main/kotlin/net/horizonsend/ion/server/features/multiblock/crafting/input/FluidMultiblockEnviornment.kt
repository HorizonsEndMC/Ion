package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock

interface FluidMultiblockEnviornment : RecipeEnviornment {
	val fluidStore: FluidStoringMultiblock get() = multiblock as FluidStoringMultiblock
}
