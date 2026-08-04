package net.horizonsend.ion.server.features.transport.fluids.properties.type

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Pressure
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Pressure.Companion.DEFAULT_PRESSURE
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Pressure.Companion.PRESSURE
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object PressureProperty : FluidPropertyType<Pressure>() {
	override val key: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<Pressure>> = FluidPropertyTypeKeys.PRESSURE

	override fun handleCombination(currentProperty: Pressure, currentAmount: Double, other: Pressure?, otherAmount: Double, location: Location?): Pressure {
		if (otherAmount <= 0.0) return currentProperty

		val newVolume = currentAmount + otherAmount

		if (newVolume <= 0.0) return Pressure(DEFAULT_PRESSURE)

		val thisPortion = currentProperty.value * (currentAmount / newVolume)
		val otherPortion = (other?.value ?: getDefaultProperty(location).value) * (otherAmount / newVolume)

		val newValue = thisPortion + otherPortion

		return Pressure(newValue.roundToHundredth())
	}

	override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): Pressure {
		return Pressure(data.getOrDefault(PRESSURE, PersistentDataType.DOUBLE, DEFAULT_PRESSURE))
	}

	override fun serialize(complex: Pressure, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		val pdc = adapterContext.newPersistentDataContainer()
		pdc.set(PRESSURE, PersistentDataType.DOUBLE, complex.value)
		return pdc
	}

	override fun getDefaultProperty(location: Location?): Pressure {
		//TODO
		return Pressure(DEFAULT_PRESSURE)
	}

	override fun formatValue(property: Pressure): Component {
		return ofChildren(Component.text(property.value.roundToHundredth()), Component.space(), Component.text("mb", HE_MEDIUM_GRAY))
	}

	override fun getDisplayName(): Component {
		return Component.text("Pressure")
	}
}
