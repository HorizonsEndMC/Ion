package net.horizonsend.ion.server.features.transport.fluids

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidPropertyKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class FluidStack(
	type: FluidType,
	amount: Double,
	private val dataComponents: MutableMap<FluidPropertyKeys.FluidPropertyKey<out FluidProperty>, FluidProperty> = Object2ObjectOpenHashMap()
) {
	var amount: Double = amount
		@Synchronized
		get
		@Synchronized
		set(value) {
			if (value < 0.0) throw IllegalArgumentException("Fluid stacks cannot have amounts less than 0!")
			if (!value.isFinite()) throw IllegalArgumentException("Fluid stacks must have a rational amount!")

			if (value == 0.0) {
				type = FluidTypeKeys.EMPTY.getValue()
			}

			field = value
		}

	var type: FluidType = type
		@Synchronized
		get
		@Synchronized
		set

	fun isEmpty(): Boolean = type.key == FluidTypeKeys.EMPTY || amount <= 0

	/**
	 * Returns a copy of this fluid stack with the amount specified
	 **/
	fun asAmount(amount: Double) = FluidStack(type, amount)

	fun clone(): FluidStack {
		return FluidStack(type, amount, dataComponents)
	}

	fun <T : FluidProperty> setData(key: FluidPropertyKeys.FluidPropertyKey<T>, data: T) {
		dataComponents[key] = data
	}

	fun <T : FluidProperty> getData(key: FluidPropertyKeys.FluidPropertyKey<T>) : T? {
		return dataComponents[key]?.let(key::castUnsafe)
	}

	fun <T : FluidProperty> getDataOrThrow(key: FluidPropertyKeys.FluidPropertyKey<T>) : T {
		return dataComponents[key]?.let(key::castUnsafe) ?: throw NullPointerException()
	}

	fun <T : FluidProperty> hasData(key: FluidPropertyKeys.FluidPropertyKey<T>) : Boolean {
		return dataComponents.keys.contains(key)
	}

	companion object : PersistentDataType<PersistentDataContainer, FluidStack> {
		fun empty() = FluidStack(FluidTypeKeys.EMPTY.getValue(), 0.0)

		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<FluidStack> = FluidStack::class.java

		override fun toPrimitive(
			complex: FluidStack,
			context: PersistentDataAdapterContext,
		): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()

			pdc.set(NamespacedKeys.FLUID_AMOUNT, PersistentDataType.DOUBLE, complex.amount)
			pdc.set(NamespacedKeys.FLUID_TYPE, FluidTypeKeys.serializer, complex.type.key)

			return pdc
		}

		override fun fromPrimitive(
			primitive: PersistentDataContainer,
			context: PersistentDataAdapterContext,
		): FluidStack {
			return FluidStack(
				primitive.get(NamespacedKeys.FLUID_TYPE, FluidTypeKeys.serializer)!!.getValue(),
				primitive.get(NamespacedKeys.FLUID_AMOUNT, PersistentDataType.DOUBLE)!!
			)
		}
	}

	override fun toString(): String {
		return "FluidStack[type=${type.key},amount=$amount]"
	}
}
