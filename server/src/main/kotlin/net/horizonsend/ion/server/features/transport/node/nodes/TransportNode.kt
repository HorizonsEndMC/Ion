package net.horizonsend.ion.server.features.transport.node.nodes

import kotlinx.serialization.SerializationException
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_DATA
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_TYPE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

/**
 * Represents a single node, or step, in transport transportNetwork
 **/
interface TransportNode : PDCSerializable<TransportNode, TransportNode.Companion> {
	override val serializationType: Companion get() = Companion

	/**
	 * The neighboring nodes that this node may transport to
	 **/
	val transferableNeighbors: MutableSet<TransportNode>

	/**
	 * Returns whether this node may transport to the provided node
	 **/
	fun isTransferable(position: Long, node: TransportNode): Boolean

	/**
	 * Store additional required data in the serialized container
	 **/
	fun storeData(persistentDataContainer: PersistentDataContainer)

	companion object : PersistentDataType<PersistentDataContainer, TransportNode> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType() = TransportNode::class.java

		override fun toPrimitive(complex: TransportNode, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			pdc.set(NODE_TYPE, NodeType.type, NodeType[complex])

			val requiredData = context.newPersistentDataContainer()

			complex.storeData(requiredData)
			pdc.set(NODE_DATA, PersistentDataType.TAG_CONTAINER, requiredData)

			return pdc
		}

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): TransportNode = try {
			val type = primitive.get(NODE_TYPE, NodeType.type)!!

			val requiredData = primitive.getOrDefault(NODE_DATA, PersistentDataType.TAG_CONTAINER, context.newPersistentDataContainer())
			type.load(requiredData)
		} catch (e: Throwable) {
			e.printStackTrace()
			throw SerializationException("Error deserializing multiblock data!")
		}
	}

	data class NodeData<T: Any>(val field: KClass<T>, val data: T)
}
