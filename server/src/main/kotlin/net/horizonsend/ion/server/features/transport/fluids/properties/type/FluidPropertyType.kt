package net.horizonsend.ion.server.features.transport.fluids.properties.type

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
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

	fun handleCombinationUnsafe(currentProperty: FluidProperty, currentAmount: Double, other: FluidProperty?, otherAmount: Double): T {
		@Suppress("UNCHECKED_CAST")
		return handleCombination(currentProperty as T, currentAmount, other as T?, otherAmount)
	}

	abstract fun handleCombination(currentProperty: T, currentAmount: Double, other: T?, otherAmount: Double): T

	fun handleCombination(stackOne: FluidStack, stackTwo: FluidStack) {
		if (stackOne.hasData(this)) {
			val combined = stackOne.getDataOrThrow(this).combine(stackOne.amount, stackTwo.getData(this), stackTwo.amount)
			stackOne.setData(this, castUnsafe(combined))
			return
		}

		if (stackTwo.hasData(this)) {
			val combined = stackTwo.getDataOrThrow(this).combine(stackTwo.amount, stackOne.getData(this), stackOne.amount)
			stackOne.setData(this, castUnsafe(combined))
			return
		}
	}
}
