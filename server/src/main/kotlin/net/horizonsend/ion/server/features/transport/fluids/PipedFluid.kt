package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory

abstract class PipedFluid(val identifier: String) {
	abstract val categories: Array<FluidCategory>

	override fun toString(): String {
		return "FLUID[$identifier]"
	}
}
