package net.horizonsend.ion.server.features.transport.fluids.properties.type

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

/**
 * Represents a type of property. Handles the serialization, combination, and provides a key for types.
 **/
abstract class FluidPropertyType<T : FluidProperty> {
	abstract val key: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<T>>

	@Suppress("UNCHECKED_CAST")
	fun castUnsafe(property: FluidProperty) : T = property as T

	fun serializeUnsafe(complex: FluidProperty, adapterContext: PersistentDataAdapterContext) = serialize(castUnsafe(complex), adapterContext)
	abstract fun serialize(complex: T, adapterContext: PersistentDataAdapterContext): PersistentDataContainer
	abstract fun deserialize(data: PersistentDataContainer, adapterContext: PersistentDataAdapterContext): T

	/**
	 * Combines this property with the given other property.
	 *
	 * The other property may be null if the fluid stack being merged does not contain this property
	 **/
	abstract fun handleCombination(currentProperty: T, currentAmount: Double, other: T?, otherAmount: Double, location: Location?): T

	/**
	 * Combines this property with the given other property.
	 *
	 * The other property may be null if the fluid stack being merged does not contain this property
	 **/
	fun handleCombination(stackOne: FluidStack, stackTwo: FluidStack, location: Location?) {
		if (stackOne.hasData(this)) {
			val stackOneData = stackOne.getDataOrThrow(this)
			val combined = handleCombination(stackOneData, stackOne.amount, stackTwo.getData(this), stackTwo.amount, location)

			stackOne.setData(this, castUnsafe(combined))
			return
		}

		if (stackTwo.hasData(this)) {
			val stackTwoData = stackTwo.getDataOrThrow(this)
			val combined = handleCombination(stackTwoData, stackOne.amount, stackTwo.getData(this), stackTwo.amount, location)

			stackOne.setData(this, castUnsafe(combined))
			return
		}
	}

	protected abstract fun getDefaultProperty(location: Location?): T
}
