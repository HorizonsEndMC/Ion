package net.horizonsend.ion.server.miscellaneous.registrations.persistence

import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.transport.filters.FilterData.FilterDataSerializer
import net.horizonsend.ion.server.features.transport.filters.FilterMeta
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
	val EMPTY_FILTER_META = register(FilterMeta.EmptyFilterMeta)
	val ITEM_FILTER_META = register(FilterMeta.ItemFilterMeta)

	val BYTE = register(delegatedType("BYTE", PersistentDataType.BYTE))
	val SHORT = register(delegatedType("SHORT", PersistentDataType.SHORT))
	val INTEGER = register(delegatedType("INTEGER", PersistentDataType.INTEGER))
	val LONG = register(delegatedType("LONG", PersistentDataType.LONG))
	val FLOAT = register(delegatedType("FLOAT", PersistentDataType.FLOAT))
	val DOUBLE = register(delegatedType("DOUBLE", PersistentDataType.DOUBLE))
	val BOOLEAN = register(delegatedType("BOOLEAN", PersistentDataType.BOOLEAN))
	val STRING = register(delegatedType("STRING", PersistentDataType.STRING))
	val BYTE_ARRAY = register(delegatedType("BYTE_ARRAY", PersistentDataType.BYTE_ARRAY))
	val INTEGER_ARRAY = register(delegatedType("INTEGER_ARRAY", PersistentDataType.INTEGER_ARRAY))
	val LONG_ARRAY = register(delegatedType("LONG_ARRAY", PersistentDataType.LONG_ARRAY))

	val TAG_CONTAINER = register(delegatedType("TAG_CONTAINER", PersistentDataType.TAG_CONTAINER))

	val BYTE_LIST = register(delegatedType("BYTE_LIST", PersistentDataType.LIST.bytes()))
	val SHORT_LIST = register(delegatedType("SHORT_LIST", PersistentDataType.LIST.shorts()))
	val INTEGER_LIST = register(delegatedType("INTEGER_LIST", PersistentDataType.LIST.integers()))
	val LONG_LIST = register(delegatedType("LONG_LIST", PersistentDataType.LIST.longs()))
	val FLOAT_LIST = register(delegatedType("FLOAT_LIST", PersistentDataType.LIST.floats()))
	val DOUBLE_LIST = register(delegatedType("DOUBLE_LIST", PersistentDataType.LIST.doubles()))
	val BOOLEAN_LIST = register(delegatedType("BOOLEAN_LIST", PersistentDataType.LIST.booleans()))
	val STRING_LIST = register(delegatedType("STRING_LIST", PersistentDataType.LIST.strings()))
	val BYTE_ARRAY_LIST = register(delegatedType("BYTE_ARRAY_LIST", PersistentDataType.LIST.byteArrays()))
	val INTEGER_ARRAY_LIST = register(delegatedType("INTEGER_ARRAY_LIST", PersistentDataType.LIST.integerArrays()))
	val LONG_ARRAY_LIST = register(delegatedType("LONG_ARRAY_LIST", PersistentDataType.LIST.longArrays()))
	val TAG_CONTAINER_LIST = register(delegatedType("TAG_CONTAINER_LIST", PersistentDataType.LIST.dataContainers()))

	val FLUID_TYPE = register(delegatedType("FLUID_TYPE", IonRegistries.FLUID_TYPE.getKeySet().serializer))
	val ATMOSPHERIC_GAS = register(delegatedType("ATMOSPHERIC_GAS", IonRegistries.ATMOSPHERIC_GAS.getKeySet().serializer))
	val CUSTOM_ITEMS = register(delegatedType("CUSTOM_ITEMS", IonRegistries.CUSTOM_ITEMS.getKeySet().serializer))
	val CUSTOM_BLOCKS = register(delegatedType("CUSTOM_BLOCKS", IonRegistries.CUSTOM_BLOCKS.getKeySet().serializer))
	val ITEM_MODIFICATIONS = register(delegatedType("ITEM_MODIFICATIONS", IonRegistries.ITEM_MODIFICATIONS.getKeySet().serializer))
	val MULTIBLOCK_RECIPE = register(delegatedType("MULTIBLOCK_RECIPE", IonRegistries.MULTIBLOCK_RECIPE.getKeySet().serializer))
	val SEQUENCE_PHASE = register(delegatedType("SEQUENCE_PHASE", IonRegistries.SEQUENCE_PHASE.getKeySet().serializer))
	val SEQUENCE = register(delegatedType("SEQUENCE", IonRegistries.SEQUENCE.getKeySet().serializer))

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

	fun <T : Any> unpack(metaDataContainer: MetaDataContainer<*, *>): T {
		@Suppress("UNCHECKED_CAST")
		return metaDataContainer.data as T
	}

	private inline fun <reified P : Any, reified C : Any> delegatedType(identifier: String, delegate: PersistentDataType<P, C>): RegisteredSerializer<C> =
		object : RegisteredSerializer<C>(identifier, C::class) {
			override fun toPrimitive(complex: C, context: PersistentDataAdapterContext): PersistentDataContainer {
				val box = context.newPersistentDataContainer()
				box.set(NamespacedKeys.CONTENT, delegate, complex)
				return box
			}

			override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): C {
				return primitive.get(NamespacedKeys.CONTENT, delegate)!!
			}
		}

	private inline fun <reified P : Any, reified C : Any, reified F : Any> mappedDelegate(
		identifier: String,
		delegate: PersistentDataType<P, C>,
		crossinline toDelegate: (F) -> C,
		crossinline fromDelegate: (C) -> F,
	): RegisteredSerializer<F> =
		object : RegisteredSerializer<F>(identifier, F::class) {
			override fun toPrimitive(complex: F, context: PersistentDataAdapterContext): PersistentDataContainer {
				val box = context.newPersistentDataContainer()
				box.set(NamespacedKeys.CONTENT, delegate, toDelegate(complex))
				return box
			}

			override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): F {
				return fromDelegate(primitive.get(NamespacedKeys.CONTENT, delegate)!!)
			}
		}
}
