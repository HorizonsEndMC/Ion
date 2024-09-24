package net.horizonsend.ion.server.features.transport.node

import kotlinx.serialization.SerializationException
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_TYPE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializable
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a single node, or step, in transport transportNetwork
 **/
abstract class TransportNode : PDCSerializable<TransportNode, TransportNode.Companion> {
	var isDead: Boolean = false
	abstract val manager: NodeManager
	override val persistentDataType: Companion get() = Companion
	abstract val type: NodeType

	/** Stored relationships between nodes **/
	val relationships: ConcurrentHashMap<BlockKey, NodeRelationship> = ConcurrentHashMap()

	/**
	 * Break all relations between this node and others
	 **/
	fun clearRelations() {
		relationships.values.forEach {
			it.breakUp()
		}
	}

	/**
	 * Create a relationship between this node and the provided node
	 *
	 * If neither side can transfer, a relation will not be created
	 **/
	fun addRelationship(point: BlockKey, other: TransportNode, offset: BlockFace) {
		// Do not add duplicates
		val existing = relationships[point]
		if (existing?.other == other) return

		NodeRelationship.create(point, this, other, offset)
		other.neighborChanged(this)
	}

	fun removeRelationship(other: TransportNode) {
		// Handle duplicate cases
		val toOther = relationships.filter { it.value.other == other }

		toOther.keys.forEach { relationships.remove(it) }

		// Notify of neighbor change
		other.neighborChanged(this)
	}

	fun removeRelationship(at: BlockKey) {
		// Handle duplicate cases
		val toOther = relationships[at]
		toOther?.breakUp()

		// Notify of neighbor change
		toOther?.other?.neighborChanged(this)
	}

	/**
	 * Returns whether this node may transport to the provided node
	 **/
	abstract fun isTransferableTo(node: TransportNode): Boolean

	/**
	 *
	 **/
	var cachedTransferable: ArrayDeque<TransportNode> = ArrayDeque(getTransferableNodes())

	fun refreshTransferCache() {
		cachedTransferable = ArrayDeque(getTransferableNodes())
		relationCache.clear()
	}

	/**
	 * Gets the distinct nodes this can transfer to
	 **/
	fun getTransferableNodes(): Collection<TransportNode> {
		return relationships.mapNotNullTo(mutableSetOf()) { relation ->
			// The other side of the relation, only if transfer is possible between this node and it. Double check if it is dead as well
			relation.value.other.takeIf { other -> relation.value.canTransfer && !other.isDead }
		}
	}

	/**
	 * Store additional required data in the serialized container
	 **/
	abstract fun storeData(persistentDataContainer: PersistentDataContainer)

	/**
	 * Load required data from the serialized container
	 **/
	abstract fun loadData(persistentDataContainer: PersistentDataContainer)

	/**
	 * Handle placement into the network upon loading, after data has been loaded
	 **/
	abstract fun loadIntoNetwork()

	/**
	 * Logic for handling the removal of this node
	 *
	 * Cleanup, splitting into multiple, etc
	 **/
	open fun handlePositionRemoval(position: BlockKey) {}

	/**
	 * Builds relations between this node and transferrable nodes
	 **/
	abstract fun buildRelations(position: BlockKey)

	/**
	 * Notify a node if a neighbor changed
	 **/
	open fun neighborChanged(neighbor: TransportNode) {}

	/**
	 * Additional logic to be run once the node is placed
	 **/
	open fun onPlace(position: BlockKey) {}

	/**
	 *
	 **/
	abstract fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int

	private val relationCache = mutableMapOf<TransportNode, Map<BlockKey, NodeRelationship>>()

	fun getRelationshipWith(other: TransportNode): Map<BlockKey, NodeRelationship> {
		return relationCache.getOrPut(other) { relationships.filter { it.value.other == other } }
	}

	/**
	 * Get the center of this node, for display and pathfinding.
	 **/
	abstract fun getCenter(): Vec3i

	/**
	 * Get the distance between the nodes by using their relative center.
	 **/
	fun getDistance(previous: TransportNode): Double {
		return previous.getCenter().distance(getCenter())
	}

	//<editor-fold desc="PDC">
	companion object : PersistentDataType<PersistentDataContainer, TransportNode> {
		override fun getPrimitiveType() = PersistentDataContainer::class.java
		override fun getComplexType() = TransportNode::class.java

		override fun toPrimitive(complex: TransportNode, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			pdc.set(NODE_TYPE, NodeType.type, complex.type)

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

		fun load(primitive: PersistentDataContainer, network: NodeManager): TransportNode = try {
			val type = primitive.get(NODE_TYPE, NodeType.type)!!

			val instance = type.newInstance(network)

			instance.loadData(primitive)

			instance
		} catch (e: Throwable) {
			throw SerializationException("Error deserializing multiblock data!", e)
		}
	}
	//</editor-fold>
}
