package net.horizonsend.ion.server.features.transport.fluids.properties.type

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock.Companion.formatProgressString
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Salinity
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Salinity.Companion.DEFAULT_SALINITY
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Salinity.Companion.SALINITY
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object SalinityProperty : FluidPropertyType<Salinity>() {
	override val key: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<Salinity>> = FluidPropertyTypeKeys.SALINITY

	override fun handleCombination(currentProperty: Salinity, currentAmount: Double, other: Salinity?, otherAmount: Double, location: Location?): Salinity {
		if (otherAmount <= 0.0) return currentProperty

		val newVolume = currentAmount + otherAmount

		if (newVolume <= 0.0) return Salinity(DEFAULT_SALINITY)

		val thisPortion = currentProperty.value * (currentAmount / newVolume)
		val otherPortion = (other?.value ?: getDefaultProperty(location).value) * (otherAmount / newVolume)

		val newValue = thisPortion + otherPortion

		return Salinity(newValue)
	}

	override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): Salinity {
		return Salinity(data.getOrDefault(SALINITY, PersistentDataType.DOUBLE, DEFAULT_SALINITY))
	}

	override fun serialize(complex: Salinity, adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		val pdc = adapterContext.newPersistentDataContainer()
		pdc.set(SALINITY, PersistentDataType.DOUBLE, complex.value)
		return pdc
	}

	override fun getDefaultProperty(location: Location?): Salinity {
		//TODO
		return Salinity(DEFAULT_SALINITY)
	}

	override fun formatValue(property: Salinity): Component {
		return ofChildren(text(formatProgressString(property.value)), text('%', HEColorScheme.HE_MEDIUM_GRAY))
	}

	override fun getDisplayName(): Component {
		return text("Salinity")
	}
}
