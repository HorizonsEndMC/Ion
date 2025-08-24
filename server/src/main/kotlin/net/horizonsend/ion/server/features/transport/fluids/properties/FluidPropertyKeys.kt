package net.horizonsend.ion.server.features.transport.fluids.properties

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.core.registration.IonResourceKey
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Pressure
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Temperature
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object FluidPropertyKeys {
	private val byNamespacedKey = Object2ObjectOpenHashMap<NamespacedKey, FluidPropertyKey<*>>()

	class FluidPropertyKey<T : FluidProperty>(key: String, val deserializer: (PersistentDataContainer, PersistentDataAdapterContext) -> T) : IonResourceKey<T>(key) {
		@Suppress("UNCHECKED_CAST")
		fun castUnsafe(property: FluidProperty) : T = property as T
	}

	private fun <T : FluidProperty> key(keyString: String, deserializer: (PersistentDataContainer, PersistentDataAdapterContext) -> T): FluidPropertyKey<T> {
		val key = FluidPropertyKey(keyString, deserializer)
		byNamespacedKey[key.ionNapespacedKey] = key
		return key
	}

	val PRESSURE = key<Pressure>("PRESSURE") { raw, context -> Pressure(raw.getOrDefault(Pressure.PRESSURE, PersistentDataType.DOUBLE, Pressure.DEFAULT_PRESSURE)) }
	val TEMPERATURE = key<Temperature>("TEMPERATURE") { raw, context -> Temperature(raw.getOrDefault(Temperature.TEMPERATURE, PersistentDataType.DOUBLE, Temperature.DEFAULT_TEMPERATURE)) }

	operator fun get(namespacedKey: NamespacedKey): FluidPropertyKey<out FluidProperty> = byNamespacedKey[namespacedKey]!!
}
