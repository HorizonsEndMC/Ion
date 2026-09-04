package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock

interface ProgressEnvironment : SingleItemResultEnvironment {
	fun getProgressManager(): ProgressMultiblock.ProgressManager
}
