package net.horizonsend.ion.server.features.transport.node

import kotlinx.serialization.SerializationException
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_TYPE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializable
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * Represents a single node, or step, in transport transportNetwork
 **/
abstract class TransportNode : PDCSerializable<TransportNode, TransportNode.Companion> {
	var isDead: Boolean = false
	abstract val manager: NodeManager
	override val persistentDataType: Companion get() = Companion
	abstract val type: NodeType

	val relationHolder = RelationHolder(this)

	/**
	 * Break all relations between this node and others
	 **/
	fun clearRelations() = relationHolder.clear()

	fun removeRelationship(other: TransportNode) = relationHolder.removeRelationship(other)

	fun removeRelationships(at: BlockKey) = relationHolder.removeAll(at)

	fun addRelationship(other: TransportNode, holderPosition: BlockKey, otherPosition: BlockKey, nodeTwoOffset: BlockFace) =
		relationHolder.addRelationship(other, holderPosition, otherPosition, nodeTwoOffset)

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
		return relationHolder.getAllOthers().mapNotNullTo(mutableSetOf()) { relation ->
			// The other side of the relation, only if transfer is possible between this node and it. Double check if it is dead as well
			relation.other.takeIf { relation.canTransfer && !it.isDead }
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
	 * The directions in which to try ro build relations
	 **/
	protected open val relationOffsets = ADJACENT_BLOCK_FACES

	/**
	 * Builds relations between this node and transferrable nodes
	 **/
	open fun buildRelations(position: BlockKey) {
		for (offset in relationOffsets) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = manager.getNode(offsetKey) ?: continue

			if (this == neighborNode) continue

			// Add a relationship, if one should be added
			addRelationship(neighborNode, position, offsetKey, offset)
		}
	}

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
		return relationCache.getOrPut(other) {
			val cache = mutableMapOf<BlockKey, NodeRelationship>()
			for ((key, relations) in relationHolder.raw()) {
				if (!relationHolder.hasRelationAtWith(key, other)) continue
				cache[key] = relations.firstOrNull { it.other == other } ?: continue
			}

			cache
		}
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
