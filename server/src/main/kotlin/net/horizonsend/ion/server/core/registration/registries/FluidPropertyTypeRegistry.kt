package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.features.transport.fluids.properties.type.FluidPropertyType
import net.horizonsend.ion.server.features.transport.fluids.properties.type.SalinityProperty
import net.horizonsend.ion.server.features.transport.fluids.properties.type.TemperatureProperty

class FluidPropertyTypeRegistry : Registry<FluidPropertyType<*>>(RegistryKeys.FLUID_PROPERTY_TYPE) {
	override fun getKeySet(): FluidPropertyTypeKeys = FluidPropertyTypeKeys

	override fun boostrap() {
		register(FluidPropertyTypeKeys.TEMPERATURE, TemperatureProperty)
		register(FluidPropertyTypeKeys.SALINITY, SalinityProperty)
	}
}
