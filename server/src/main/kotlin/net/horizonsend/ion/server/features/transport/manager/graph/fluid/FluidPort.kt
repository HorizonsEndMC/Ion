package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.transport.fluids.FluidStack

class FluidPort(val throughputPerTick: Double, val entity: FluidStoringMultiblock) {
	fun getExtractable(): List<FluidStack> {
		return entity.getRemovable().map { container -> container.getContents() }
	}

	fun canAdd(stack: FluidStack): Boolean {
		return entity.canAdd(stack)
	}
}
