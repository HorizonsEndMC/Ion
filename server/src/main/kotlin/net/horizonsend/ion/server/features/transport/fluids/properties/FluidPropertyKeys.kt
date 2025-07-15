package net.horizonsend.ion.server.features.transport.fluids.properties

import net.horizonsend.ion.server.core.registration.IonResourceKey

object FluidPropertyKeys {
	class FluidPropertyKey<T : FluidProperty>(key: String) : IonResourceKey<T>(key) {
		@Suppress("UNCHECKED_CAST")
		fun castUnsafe(property: FluidProperty) : T = property as T
	}

	private fun <T : FluidProperty> key(key: String): FluidPropertyKey<T> = FluidPropertyKey(key)

	val PRESSURE = key<FluidProperty.Pressure>("PRESSURE")
	val TEMPERATURE = key<FluidProperty.Temperature>("TEMPERATURE")
}
