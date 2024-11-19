package net.horizonsend.ion.server.features.transport.fluids

data class FluidStack(
	var type: Fluid,
	var amount: Int
) : Cloneable
