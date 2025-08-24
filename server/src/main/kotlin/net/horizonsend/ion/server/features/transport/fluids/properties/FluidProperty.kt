package net.horizonsend.ion.server.features.transport.fluids.properties

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.properties.type.FluidPropertyType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys

interface FluidProperty {
	val typeKey: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<*>>

	fun clone(): FluidProperty

	data class Pressure(val value: Double) : FluidProperty {
		init {
		    check(value.isFinite()) { "Pressure must be finite!" }
		}

		override val typeKey: IonRegistryKey<FluidPropertyType<*>, FluidPropertyType<Pressure>> = FluidPropertyTypeKeys.PRESSURE

		companion object {
			val PRESSURE = NamespacedKeys.key("pressure")
			const val DEFAULT_PRESSURE = 0.0
		}

		override fun clone(): Pressure {
			return copy()
		}
	}

	data class Temperature(val value: Double) : FluidProperty {
		init {
			check(value.isFinite()) { "Temperature must be finite!" }
		}

		override val typeKey: IonRegistryKey<FluidPropertyType<*>, FluidPropertyType<Temperature>> = FluidPropertyTypeKeys.TEMPERATURE

		companion object {
			val TEMPERATURE = NamespacedKeys.key("temperature")
			const val DEFAULT_TEMPERATURE = 15.0
		}

		override fun clone(): Temperature {
			return copy()
		}
	}
}
