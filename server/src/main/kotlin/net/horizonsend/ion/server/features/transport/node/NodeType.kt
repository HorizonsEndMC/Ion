package net.horizonsend.ion.server.features.transport.node

import com.manya.pdc.base.EnumDataType
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import kotlinx.serialization.SerializationException
import net.horizonsend.ion.server.features.transport.node.nodes.SpongeNode
import net.horizonsend.ion.server.features.transport.node.nodes.TransportNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

enum class NodeType(val javaClass: Class<out TransportNode>, val kotlinClass: KClass<out TransportNode>, registerDataLoaders: NodeType.() -> Unit = {}) {
	// GENERAL
	//TODO Extractor
	//TODO Straight node for power and gas
	//TODO Merge node

	// POWER
	SPONGE_NODE(SpongeNode::class.java, SpongeNode::class, {
		addDataLoader(SpongeNode::positions, NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, MutableSet::class.java) { LongOpenHashSet(it) }
	}),
	//TODO Power flow meter
	//TODO Power input
	//TODO Power Splitter

	// GAS
	//TODO pane node

	// ITEM
	//TODO Sponge equivalent for glass color
	;

	private val dataLoaders: MutableMap<KProperty<*>, DataLoader<*, *, *>> = mutableMapOf()

	fun <J, Z, T> addDataLoader(
		property: KProperty<J>,
		storageKey: NamespacedKey,
		dataType: PersistentDataType<T, Z>,
		fieldDataClass: Class<J>,
		defaultValue: Z? = null,
		complexTransform: (Z?) -> J
	) {
		dataLoaders[property] = DataLoader(storageKey, dataType, fieldDataClass, defaultValue, complexTransform)
	}

	fun load(additionalData: PersistentDataContainer): TransportNode {
		val fields: Array<Class<out Any>> = dataLoaders.map { it.value.fieldDataClass }.toTypedArray()
		val data: Array<out Any?> = dataLoaders.map { (_, loader) -> loader.get(additionalData) }.toTypedArray()

		return javaClass.getDeclaredConstructor(*fields).newInstance(data)
	}

	init {
	    registerDataLoaders(this)
	}

	companion object {
		val type = EnumDataType(NodeType::class.java)
		private val byNode: Map<Class<out TransportNode>, NodeType> = entries.associateBy { it.javaClass }

		operator fun get(node: TransportNode): NodeType = byNode[node.javaClass] ?: throw NoSuchElementException("Unregistered node type ${node.javaClass.simpleName}!")
	}

	/**
	 * @param key The key that the data will be stored in
	 * @param dataType The persistent data type of the type
	 * @param fieldDataClass The Java class of the type of the field
	 * @param defaultValue The default value if none is found from serialization. If not provided, and no data is found, a serialization exception will occur
	 * @param complexTransform The transform applied to the value retrieved from the persistent data container
	 *
	 * @param J the final type
	 * @param T the serialization complex type
	 * @param Z the serialization primitive type
	 **/
	private class DataLoader<J, Z, T>(
		val key: NamespacedKey,
		val dataType: PersistentDataType<T, Z>,
		val fieldDataClass: Class<J>,
		val defaultValue: Z?,
		val complexTransform: (Z?) -> J
	) {
		fun get(container: PersistentDataContainer): J {
			val raw = container.get(key, dataType) ?: defaultValue ?: throw SerializationException()

			return complexTransform(raw)
		}
	}
}
