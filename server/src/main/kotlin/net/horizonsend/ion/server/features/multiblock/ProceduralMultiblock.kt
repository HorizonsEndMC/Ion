package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape

abstract class ProceduralMultiblock : Multiblock() {
	protected val combinedShape = MultiblockShape().apply { buildExampleShape() }
	protected abstract fun MultiblockShape.buildExampleShape()

	override fun getExampleShape(): MultiblockShape {
		return combinedShape
	}
}
