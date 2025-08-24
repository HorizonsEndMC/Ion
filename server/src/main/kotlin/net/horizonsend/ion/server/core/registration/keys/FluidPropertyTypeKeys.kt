package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.FLUID_PROPERTY_TYPE
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.properties.type.FluidPropertyType

object FluidPropertyTypeKeys : KeyRegistry<FluidPropertyType<*>>(FLUID_PROPERTY_TYPE, FluidPropertyType::class) {
	val TEMPERATURE = registerTypedKey<FluidPropertyType<FluidProperty.Temperature>>("TEMPERATURE")
	val PRESSURE = registerTypedKey<FluidPropertyType<FluidProperty.Pressure>>("PRESSURE")
}
