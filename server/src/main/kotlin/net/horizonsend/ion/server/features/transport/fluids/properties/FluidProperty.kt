package net.horizonsend.ion.server.features.transport.fluids.properties

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.type.FluidPropertyType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys

interface FluidProperty {
	val typeKey: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<*>>

	fun clone(): FluidProperty

	/**
	 * Stores temperature, in Celsius
	 **/
	data class Temperature(val value: Double) : FluidProperty {
		init {
			check(value.isFinite()) { "Temperature must be finite!" }
		}

		override val typeKey: IonRegistryKey<FluidPropertyType<*>, FluidPropertyType<Temperature>> = FluidPropertyTypeKeys.TEMPERATURE

		companion object {
			val TEMPERATURE = NamespacedKeys.key("temperature")
			const val DEFAULT_TEMPERATURE = 15.0
		}

		override fun clone(): Temperature = copy()
	}

	data class Salinity(val value: Double) : FluidProperty {
		init {
			check(value.isFinite()) { "Temperature must be finite!" }
			check(value in 0.0..1.0) { "Salinity must be between 0.0 and 1.0!" }
		}

		override val typeKey: IonRegistryKey<FluidPropertyType<*>, FluidPropertyType<Salinity>> = FluidPropertyTypeKeys.SALINITY

		companion object {
			val SALINITY = NamespacedKeys.key("salinity")
			const val DEFAULT_SALINITY = 0.0
		}

		override fun clone(): Salinity = copy()
	}

	data class Flammability(
		val joulesPerLiter: Double,
		val resultFluid: IonRegistryKey<FluidType, out FluidType>,
		val resultVolumeMultiplier: Double
	) : FluidProperty {
		init {
			check(joulesPerLiter.isFinite() && joulesPerLiter >= 0.0) { "Joules per liter must be between positive and finite!" }
			check(joulesPerLiter.isFinite() && joulesPerLiter >= 0.0) { "Result volume multiplier must be between positive and finite!" }
		}

		override val typeKey: IonRegistryKey<FluidPropertyType<*>, FluidPropertyType<Flammability>> = FluidPropertyTypeKeys.FLAMMABILITY

		override fun clone(): Flammability = copy()

	}
}
