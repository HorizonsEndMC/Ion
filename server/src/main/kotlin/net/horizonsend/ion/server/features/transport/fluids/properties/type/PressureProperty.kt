package net.horizonsend.ion.server.features.transport.fluids.properties.type

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Pressure
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Pressure.Companion.DEFAULT_PRESSURE
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Pressure.Companion.PRESSURE
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object PressureProperty : FluidPropertyType<Pressure>() {
	override val key: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<Pressure>> = FluidPropertyTypeKeys.PRESSURE

	override fun handleCombination(currentProperty: Pressure, currentAmount: Double, other: Pressure?, otherAmount: Double) {
		val newVolume = currentAmount + otherAmount
		val thisPortion = currentProperty.value * (currentAmount / newVolume)
		val otherPortion = (other?.value ?: DEFAULT_PRESSURE) * (otherAmount / newVolume)

		currentProperty.value = thisPortion + otherPortion
	}

	override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): Pressure {
		return Pressure(data.getOrDefault(PRESSURE, PersistentDataType.DOUBLE, DEFAULT_PRESSURE))
	}

	override fun serialize(complex: Pressure, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		val pdc = adapterContext.newPersistentDataContainer()
		pdc.set(PRESSURE, PersistentDataType.DOUBLE, complex.value)
		return pdc
	}
}
