package net.horizonsend.ion.server.features.transport.node

import kotlinx.serialization.SerializationException
import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_TYPE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializable
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ThreadLocalRandom

/**
 * Represents a single node, or step, in transport transportNetwork
 **/
interface TransportNode : PDCSerializable<TransportNode, TransportNode.Companion> {
	var isDead: Boolean
	val network: TransportNetwork
	override val persistentDataType: Companion get() = Companion

	/**
	 * Stored relationships between nodes
	 **/
	val relationships: MutableSet<NodeRelationship>

	/**
	 * Break all relations between this node and others
	 **/
	suspend fun clearRelations() {
		relationships.forEach {
			it.breakUp()
		}
	}

	/**
	 * Create a relationship between this node and the provided node
	 *
	 * If neither side can transfer, a relation will not be created
	 **/
	suspend fun addRelationship(other: TransportNode, offset: BlockFace) {
		// Do not add duplicates
		if (relationships.any { it.sideTwo.node == other }) return

		NodeRelationship.create(this, other, offset)
		other.neighborChanged(this)
	}

	suspend fun removeRelationship(other: TransportNode) {
		// Handle duplicate cases
		val toOther = relationships.filterTo(mutableSetOf()) { it.sideTwo.node == other }

		relationships.removeAll(toOther)
		other.neighborChanged(this)
	}

	/**
	 * Returns whether this node may transport to the provided node
	 **/
	fun isTransferableTo(node: TransportNode): Boolean

	/** Gets the nodes this can transfer to **/
	fun getTransferableNodes(): Collection<Pair<TransportNode, BlockFace>> = relationships.filter {
		// That this node can transfer to the other
		it.sideOne.transferAllowed && !it.sideTwo.node.isDead
	}.map { it.sideTwo.node to it.sideOne.offset }.shuffled(ThreadLocalRandom.current())

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
	 * Notify a node if a neighbor changed
	 **/
	suspend fun neighborChanged(neighbor: TransportNode) {}

	/**
	 * Additional logic to be run once the node is placed
	 **/
	suspend fun onPlace(position: BlockKey) {}

	/**
	 * Logic for the completion of a power transfer
	 **/
	suspend fun onCompleteChain(final: BranchHead<*>, destination: PowerInputNode, transferred: Int) {}

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

		fun load(primitive: PersistentDataContainer, network: TransportNetwork): TransportNode = try {
			val type = primitive.get(NODE_TYPE, NodeType.type)!!

			val instance = type.newInstance(network)

			instance.loadData(primitive)

			instance
		} catch (e: Throwable) {
			throw SerializationException("Error deserializing multiblock data!", e)
		}
	}
}
