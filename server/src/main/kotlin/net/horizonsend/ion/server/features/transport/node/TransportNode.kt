package net.horizonsend.ion.server.features.transport.node

import kotlinx.serialization.SerializationException
import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_TYPE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializable
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * Represents a single node, or step, in transport transportNetwork
 **/
interface TransportNode : PDCSerializable<TransportNode, TransportNode.Companion> {
	val network: ChunkTransportNetwork
	override val persistentDataType: Companion get() = Companion

	/**
	 * Stored relationships between nodes
	 **/
	val relationships: MutableSet<NodeRelationship>

	/**
	 * Break all relations between this node and others
	 **/
	fun clearRelations() {
		relationships.forEach { it.breakUp() }
	}

	fun addRelationship(other: TransportNode) {
		// Null if relationship was not worth it
		val relationship = NodeRelationship.create(this, other) ?: return

		relationships.add(relationship)
	}

	/**
	 * Returns whether this node may transport to the provided node
	 **/
	fun isTransferableTo(node: TransportNode): Boolean

	/**
	 * Store additional required data in the serialized container
	 **/
	fun storeData(persistentDataContainer: PersistentDataContainer)

	/**
	 * Load required data from the serialized container
	 **/
	fun loadData(persistentDataContainer: PersistentDataContainer)

	/**
	 * Handle placement into the network upon loading, after data has been loaded
	 **/
	fun loadIntoNetwork()

	/**
	 * Logic for handling the removal of this node
	 *
	 * Cleanup, splitting into multiple, etc
	 **/
	suspend fun handleRemoval(position: BlockKey) {}

	/**
	 * Builds relations between this node and transferrable nodes
	 **/
	suspend fun buildRelations(position: BlockKey)

	/**
	 * Additional logic to be run once the node is placed
	 **/
	suspend fun onPlace(position: BlockKey) {}

	/**
	 * Handle the stepping of power through this node
	 *
	 * This may create a new step for a single node, spawn off multiple steps, or more
	 * Each node defines how it is stepped.
	 **/
	suspend fun handleStep(step: Step)

	companion object : PersistentDataType<PersistentDataContainer, TransportNode> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType() = TransportNode::class.java

		override fun toPrimitive(complex: TransportNode, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			pdc.set(NODE_TYPE, NodeType.type, NodeType[complex])

			complex.storeData(pdc)

			return pdc
		}

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): TransportNode = try {
			val type = primitive.get(NODE_TYPE, NodeType.type)!!

			val instance = type.newInstance()

			instance.loadData(primitive)

			instance
		} catch (e: Throwable) {
			throw SerializationException("Error deserializing multiblock data!", e)
		}

		fun load(primitive: PersistentDataContainer, network: ChunkTransportNetwork): TransportNode = try {
			val type = primitive.get(NODE_TYPE, NodeType.type)!!

			val instance = type.newInstance(network)

			instance.loadData(primitive)

			instance
		} catch (e: Throwable) {
			throw SerializationException("Error deserializing multiblock data!", e)
		}
	}
}
