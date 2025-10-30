package net.horizonsend.ion.server.features.transport.fluids

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.properties.type.FluidPropertyType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class FluidStack(
	type: IonRegistryKey<FluidType, out FluidType>,
	amount: Double,
	private val dataComponents: MutableMap<FluidPropertyType<*>, FluidProperty> = Object2ObjectOpenHashMap()
) {
	var amount: Double = amount
		@Synchronized
		get
		@Synchronized
		set(value) {
			if (value < 0.0) throw IllegalArgumentException("Fluid stacks cannot have amounts less than 0!")
			if (!value.isFinite()) throw IllegalArgumentException("Fluid stacks must have a rational amount!")

			if (value == 0.0) {
				type = FluidTypeKeys.EMPTY
				dataComponents.clear()
			}

			field = value
		}

	var type: IonRegistryKey<FluidType, out FluidType> = type
		@Synchronized
		get
		@Synchronized
		set

	fun isEmpty(): Boolean = type == FluidTypeKeys.EMPTY || amount <= 0
	fun isNotEmpty(): Boolean = type != FluidTypeKeys.EMPTY && amount > 0

	/**
	 * Returns a copy of this fluid stack with the amount specified
	 **/
	fun asAmount(amount: Double) = FluidStack(type, amount, Object2ObjectOpenHashMap(dataComponents))

	fun clone(): FluidStack {
		return FluidStack(type, amount, Object2ObjectOpenHashMap(dataComponents))
	}

	/** Returns an immutable map of all properties contained by this fluid stack */
	fun getAllProperties(): Map<FluidPropertyType<*>, FluidProperty> = Object2ObjectOpenHashMap<FluidPropertyType<*>, FluidProperty>().apply {
		putAll(type.getValue().defaultProperties.mapKeys { it.key.getValue() })
		putAll(dataComponents) // Add patch second to override default values
	}

	fun <T : FluidProperty> setData(type: FluidPropertyType<T>, data: T): FluidStack {
		if (!type.canBeCustom()) throw IllegalArgumentException("Fluid property $type cannot be used in custom data!")
		dataComponents[type] = data
		return this
	}

	fun <T : FluidProperty> setData(type: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<T>>, data: T): FluidStack {
		if (!type.getValue().canBeCustom()) throw IllegalArgumentException("Fluid property $type cannot be used in custom data!")
		dataComponents[type.getValue()] = data
		return this
	}

	private fun setDataUnsafe(type: FluidPropertyType<*>, data: FluidProperty): FluidStack {
		if (!type.canBeCustom()) throw IllegalArgumentException("Fluid property $type cannot be used in custom data!")
		dataComponents[type] = data
		return this
	}

	fun <T : FluidProperty> getData(type: FluidPropertyType<T>) : T? {
		return getAllProperties()[type]?.let { type.castUnsafe(it) }
	}

	fun <T : FluidProperty> getData(key: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<T>>) : T? {
		return getAllProperties()[key.getValue()]?.let { key.getValue().castUnsafe(it) }
	}

	fun <T : FluidProperty> getDataOrDefault(type: FluidPropertyType<T>, location: Location?) : T {
		return getAllProperties()[type]?.let { type.castUnsafe(it) } ?: type.getDefaultProperty(location)
	}

	fun <T : FluidProperty> getDataOrDefault(type: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<T>>, location: Location?) : T {
		return getAllProperties()[type.getValue()]?.let { type.getValue().castUnsafe(it) } ?: type.getValue().getDefaultProperty(location)
	}

	fun <T : FluidProperty> getDataOrThrow(type: FluidPropertyType<T>) : T {
		return getData(type) ?: throw NullPointerException()
	}

	fun <T : FluidProperty> getDataOrThrow(type: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<T>>) : T {
		return getData(type) ?: throw NullPointerException()
	}

	fun hasData(type: FluidPropertyType<*>) : Boolean {
		return getAllProperties().keys.contains(type)
	}

	fun hasCustomData(type: FluidPropertyType<*>) : Boolean {
		return dataComponents.keys.contains(type)
	}

	fun hasData(type: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<*>>) : Boolean {
		return getAllProperties().keys.contains(type.getValue())
	}

	fun hasCustomData(type: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<*>>) : Boolean {
		return dataComponents.keys.contains(type.getValue())
	}

	fun getCustomDataMap() = dataComponents.toMap()

	/**
	 * Returns if these FluidStacks are of the same type
	 **/
	fun canCombine(other: FluidStack): Boolean {
		return other.type.key == type.key
	}

	/**
	 * Combines the properties of these two fluid stacks. Assumes that they have already been checked and can combine.
	 *
	 * The provided location is used to generate a default value if the other stack does not have this property
	 **/
	fun combine(other: FluidStack, location: Location?) {
		val existingPropertyKeys = getCustomDataMap().keys
		val otherPropertyKeys = other.getCustomDataMap().keys
		val allPropertyKeys = existingPropertyKeys.plus(otherPropertyKeys)

		for (type: FluidPropertyType<*> in allPropertyKeys) {
			type.handleCombination(this, other, location)
		}

		amount += other.amount
		if (type == FluidTypeKeys.EMPTY && other.type != FluidTypeKeys.EMPTY) {
			type = other.type
		}
		other.amount = 0.0
	}

	companion object : PersistentDataType<PersistentDataContainer, FluidStack> {
		fun empty() = FluidStack(FluidTypeKeys.EMPTY, 0.0)

		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<FluidStack> = FluidStack::class.java

		override fun toPrimitive(
			complex: FluidStack,
			context: PersistentDataAdapterContext,
		): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()

			pdc.set(NamespacedKeys.FLUID_AMOUNT, PersistentDataType.DOUBLE, complex.amount)
			pdc.set(NamespacedKeys.FLUID_TYPE, FluidTypeKeys.serializer, complex.type)

			val fluidComponents = context.newPersistentDataContainer()

			for ((type, property: FluidProperty) in complex.dataComponents) {
				val serialized = type.serializeUnsafe(property, context)

				fluidComponents.set(type.key.ionNapespacedKey, PersistentDataType.TAG_CONTAINER, serialized)
			}

			pdc.set(NamespacedKeys.FLUID_PROPERTY_COMPONENTS, PersistentDataType.TAG_CONTAINER, fluidComponents)

			return pdc
		}

		override fun fromPrimitive(
			primitive: PersistentDataContainer,
			context: PersistentDataAdapterContext,
		): FluidStack {
			val dataKeys = primitive.get(NamespacedKeys.FLUID_PROPERTY_COMPONENTS, PersistentDataType.TAG_CONTAINER)!!

			val stack = FluidStack(
				primitive.get(NamespacedKeys.FLUID_TYPE, FluidTypeKeys.serializer)!!,
				primitive.get(NamespacedKeys.FLUID_AMOUNT, PersistentDataType.DOUBLE)!!
			)

			for (key in dataKeys.keys) {
				val propertyKey = FluidPropertyTypeKeys[key]!!
				val data = dataKeys.get(key, PersistentDataType.TAG_CONTAINER) ?: continue
				val deserialized = propertyKey.getValue().deserialize(data, context)

				stack.setDataUnsafe(propertyKey.getValue(), deserialized)
			}

			return stack
		}
	}

	override fun toString(): String {
		return "FluidStack{type=${type.key},amount=$amount,properties=[${dataComponents.entries.joinToString { (key, value) -> "(${key.key}:$value)" }}]}"
	}

	fun getDisplayName(): Component = type.getValue().getDisplayName(this)
}
