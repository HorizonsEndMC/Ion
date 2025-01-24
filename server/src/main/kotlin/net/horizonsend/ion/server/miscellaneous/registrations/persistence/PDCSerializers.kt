package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import net.horizonsend.ion.server.features.transport.filters.FilterData.FilterDataSerializer
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

object PDCSerializers {
	private val registeredSerializers = mutableMapOf<String, RegisteredSerializer<*>>()
	private val typedSerialized = mutableMapOf<KClass<*>, RegisteredSerializer<*>>()

	fun <T: RegisteredSerializer<*>> register(serializer: T): T {
		registeredSerializers[serializer.identifier] = serializer
		typedSerialized[serializer.complexType] = serializer
		return serializer
	}

	val ITEM_EXTRACTOR_METADATA = register(ItemExtractorData.ItemExtractorMetaData.Companion)
	val FILTER_DATA = register(FilterDataSerializer)

	operator fun get(identifier: String) : RegisteredSerializer<*> = registeredSerializers[identifier]!!

	abstract class RegisteredSerializer<C : Any>(val identifier: String, val complexType: KClass<C>) : PersistentDataType<PersistentDataContainer, C> {
		override fun getComplexType(): Class<C> = complexType.java
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

		fun serialize(obj: Any, context: PersistentDataAdapterContext): PersistentDataContainer {
			require(complexType.isInstance(obj))
			@Suppress("UNCHECKED_CAST")
			return toPrimitive(obj as C, context)
		}

		fun deserialize(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): C {
			return fromPrimitive(primitive, context)
		}

		fun loadMetaDataContainer(data: PersistentDataContainer, context: PersistentDataAdapterContext): MetaDataContainer<C, *> {
			return MetaDataContainer(this, deserialize(data, context))
		}
	}

	fun <C : Any> pack(data: C): MetaDataContainer<C, RegisteredSerializer<C>> {
		@Suppress("UNCHECKED_CAST")
		val serializer = (typedSerialized[data::class] as? RegisteredSerializer<C>) ?: throw NoSuchElementException("No serialier found for ${data::class.simpleName}")
		return MetaDataContainer(serializer, data)
	}
}
