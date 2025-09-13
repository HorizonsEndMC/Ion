package net.horizonsend.ion.server.features.transport.fluids.properties.type

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Pressure.Companion.DEFAULT_PRESSURE
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Temperature
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Temperature.Companion.DEFAULT_TEMPERATURE
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Temperature.Companion.TEMPERATURE
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object TemperatureProperty : FluidPropertyType<Temperature>() {
	override val key: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<Temperature>> = FluidPropertyTypeKeys.TEMPERATURE

	override fun handleCombination(currentProperty: Temperature, currentAmount: Double, other: Temperature?, otherAmount: Double, location: Location?): Temperature {
		if (otherAmount <= 0.0) return currentProperty

		val newVolume = currentAmount + otherAmount

		if (newVolume <= 0.0) return Temperature(DEFAULT_TEMPERATURE)

		val thisPortion = currentProperty.value * (currentAmount / newVolume)
		val otherPortion = (other?.value ?: getDefaultProperty(location).value) * (otherAmount / newVolume)

		val newValue = thisPortion + otherPortion

		return Temperature(newValue.roundToHundredth())
	}

	override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): Temperature {
		return Temperature(data.getOrDefault(TEMPERATURE, PersistentDataType.DOUBLE, DEFAULT_TEMPERATURE))
	}

	override fun serialize(complex: Temperature, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		val pdc = adapterContext.newPersistentDataContainer()
		pdc.set(TEMPERATURE, PersistentDataType.DOUBLE, complex.value)
		return pdc
	}

	override fun getDefaultProperty(location: Location?): Temperature {
		//TODO
		return Temperature(DEFAULT_PRESSURE)
	}

	override fun formatValue(property: Temperature): Component {
		return ofChildren(Component.text(property.value.roundToHundredth()), Component.space(), Component.text("°C", HE_MEDIUM_GRAY))
	}

	override fun getDisplayName(): Component {
		return Component.text("Temperature")
	}
}
