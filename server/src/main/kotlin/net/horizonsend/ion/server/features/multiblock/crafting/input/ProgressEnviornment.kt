package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock

interface ProgressEnviornment : SingleItemResultEnviornment {
	fun getProgressManager(): ProgressMultiblock.ProgressManager
}
