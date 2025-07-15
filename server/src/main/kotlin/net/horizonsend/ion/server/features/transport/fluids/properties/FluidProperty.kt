package net.horizonsend.ion.server.features.transport.fluids.properties

interface FluidProperty {
	data class Pressure(val value: Double) : FluidProperty
	data class Temperature(val value: Double) : FluidProperty
}
