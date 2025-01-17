package net.horizonsend.ion.server.features.multiblock.newcrafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock

interface ProgressEnviornment : ItemResultEnviornment {
	fun getProgressManager(): ProgressMultiblock.ProgressManager
}
