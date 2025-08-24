package net.horizonsend.ion.server.features.transport.fluids.properties.type

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Temperature
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Temperature.Companion.DEFAULT_TEMPERATURE
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Temperature.Companion.TEMPERATURE
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object TemperatureProperty : FluidPropertyType<Temperature>() {
	override val key: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<Temperature>> = FluidPropertyTypeKeys.TEMPERATURE

	override fun handleCombination(currentProperty: Temperature, currentAmount: Double, other: Temperature?, otherAmount: Double) {
		val newVolume = currentAmount + otherAmount
		val thisPortion = currentProperty.value * (currentAmount / newVolume)
		val otherPortion = (other?.value ?: DEFAULT_TEMPERATURE) * (otherAmount / newVolume)

		currentProperty.value = thisPortion + otherPortion
	}

	override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): Temperature {
		return Temperature(data.getOrDefault(TEMPERATURE, PersistentDataType.DOUBLE, DEFAULT_TEMPERATURE))
	}

	override fun serialize(complex: Temperature, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		val pdc = adapterContext.newPersistentDataContainer()
		pdc.set(TEMPERATURE, PersistentDataType.DOUBLE, complex.value)
		return pdc
	}
}
