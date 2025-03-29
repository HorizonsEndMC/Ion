package net.horizonsend.ion.server.features.transport.fluids

data class FluidStack(
	var type: FluidType,
	var amount: Int
) : Cloneable
