package net.horizonsend.ion.server.features.transport.fluids.properties

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

interface FluidProperty {
	data class Pressure(var value: Double) : FluidProperty {
		override fun serialize(context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			pdc.set(PRESSURE, PersistentDataType.DOUBLE, value)
			return pdc
		}

		override fun combine(thisAmount: Double, other: FluidProperty?, otherAmount: Double) {
			other as Pressure?

			val newVolume = thisAmount + otherAmount
			val thisPortion = value * (thisAmount / newVolume)
			val otherPortion = (other?.value ?: DEFAULT_PRESSURE) * (otherAmount / newVolume)

			value = thisPortion + otherPortion
		}

		companion object {
			val PRESSURE = NamespacedKeys.key("pressure")
			const val DEFAULT_PRESSURE = 0.0
		}
	}
	data class Temperature(var value: Double) : FluidProperty {
		override fun serialize(context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			pdc.set(TEMPERATURE, PersistentDataType.DOUBLE, value)
			return pdc
		}

		override fun combine(thisAmount: Double, other: FluidProperty?, otherAmount: Double) {
			other as Temperature?

			val newVolume = thisAmount + otherAmount
			val thisPortion = value * (thisAmount / newVolume)
			val otherPortion = (other?.value ?: DEFAULT_TEMPERATURE) * (otherAmount / newVolume)

			value = thisPortion + otherPortion
		}

		companion object {
			val TEMPERATURE = NamespacedKeys.key("temperature")
			const val DEFAULT_TEMPERATURE = 15.0
		}
	}

	fun serialize(context: PersistentDataAdapterContext): PersistentDataContainer

	/**
	 * Combines this property with the given other property.
	 *
	 * The other property may be null if the fluid stack being merged does not contain this property
	 **/
	fun combine(thisAmount: Double, other: FluidProperty?, otherAmount: Double)
}
