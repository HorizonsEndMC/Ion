package net.horizonsend.ion.server.miscellaneous.utils.persistence

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER
import java.util.function.Consumer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

class SettingsContainer<H : Any>(
	val changeCallback: Consumer<SettingsProperty<H, Any?>> = Consumer { },
	private vararg val settingsProperties: SettingsProperty<H, Any?>,
) {
	private val propertyMap: Map<KMutableProperty1<*, *>, SettingsProperty<H, Any?>> = settingsProperties.associateBy { it.backing }

	fun save(sourceContainer: PersistentDataContainer, context: PersistentDataAdapterContext) {
		sourceContainer.set(SETTINGS_CONTAINER_KEY, TAG_CONTAINER, serialize(context))
	}

	fun serialize(context: PersistentDataAdapterContext): PersistentDataContainer {
		val pdc = context.newPersistentDataContainer()

		for (property in settingsProperties) {
			val value = getValue(property.backing) ?: continue
			pdc.set(property.key, property.serializer, value)
		}

		return pdc
	}

	fun loadData(sourceContainer: PersistentDataContainer) {
		val stored = sourceContainer.get(SETTINGS_CONTAINER_KEY, TAG_CONTAINER)

		for (property in settingsProperties) {
			val propertyValue = stored?.get(property.key, property.serializer) ?: property.defaultValue
			setValue(property.backing, propertyValue)
		}
	}

	private val storedPropertyMap = mutableMapOf<KMutableProperty1<*, *>, Any?>()

	fun <V : Any?> setValue(property: KMutableProperty1<H, out V>, value: V) {
		val propertySerializer = propertyMap[property] ?: throw IllegalStateException("Property not registered for field ${property.name}!")

		storedPropertyMap[property] = value
		changeCallback.accept(propertySerializer)
	}

	fun <V : Any?> getValue(property: KMutableProperty1<H, out V>): V {
		if (!propertyMap.containsKey(property)) throw IllegalStateException("Property not registered for field ${property.name}!")

		@Suppress("UNCHECKED_CAST")
		return storedPropertyMap[property] as V
	}

	class SettingsProperty<H : Any, out T : Any?>(val backing: KMutableProperty1<H, out T>, val serializer: PersistentDataType<*, @UnsafeVariance T>, val defaultValue: T) {
		private fun getKey(property: KMutableProperty1<*, *>): NamespacedKey {
			return NamespacedKeys.key(property.name)
		}

		val key by lazy { getKey(backing) }

		fun getValue(container: SettingsContainer<H>): T = container.getValue(backing)
	}

	companion object {
		fun <T : MultiblockEntity> multiblockSettings(data: PersistentMultiblockData, vararg settingsProperties: SettingsProperty<T, Any?>): SettingsContainer<T> {
			val container = SettingsContainer(changeCallback = {  }, *settingsProperties)
			container.loadData(data.getAdditionalDataRaw())
			return container
		}

		val SETTINGS_CONTAINER_KEY = NamespacedKeys.key("settings_container")
	}

	fun <V : Any?> getDelegate() = SettingsDelegate<V, H>(this)

	class SettingsDelegate<T : Any?, O : Any>(val container: SettingsContainer<O>) : ReadWriteProperty<O, T> {
		override fun getValue(thisRef: O, property: KProperty<*>): T {
			@Suppress("UNCHECKED_CAST")
			return container.getValue(property as KMutableProperty1<O, T>)
		}

		override fun setValue(thisRef: O, property: KProperty<*>, value: T) {
			@Suppress("UNCHECKED_CAST")
			return container.setValue(property as KMutableProperty1<O, T>, value)
		}
	}
}
