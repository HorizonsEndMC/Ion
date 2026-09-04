package net.horizonsend.ion.server.features.transport.fluids.properties.type

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

object FlammabilityProperty : FluidPropertyType<FluidProperty.Flammability>() {
	override val key: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<FluidProperty.Flammability>> = FluidPropertyTypeKeys.FLAMMABILITY
	override fun canBeCustom(): Boolean = false

	override fun serialize(complex: FluidProperty.Flammability, adapterContext: PersistentDataAdapterContext, ): PersistentDataContainer = TODO("Not yet implemented")
	override fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext, ): FluidProperty.Flammability = TODO("Not yet implemented")

	override fun handleCombination(
		currentProperty: FluidProperty.Flammability,
		currentAmount: Double,
		other: FluidProperty.Flammability?,
		otherAmount: Double,
		location: Location?,
	): FluidProperty.Flammability {
		return currentProperty // Don't bother since they can't be custom properties
	}

	override fun getDisplayName(): Component = Component.text("Flammability", NamedTextColor.RED)

	override fun formatValue(property: FluidProperty.Flammability): Component {
		TODO("Not yet implemented")
	}

	override fun getDefaultProperty(location: Location?): FluidProperty.Flammability {
		TODO("Not yet implemented")
	}
}
