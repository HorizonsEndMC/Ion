package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory

abstract class PipedFluid {
	abstract val categories: Array<FluidCategory>
}
