package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.node.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.math.roundToInt

class PowerExtractorNode(override val manager: PowerNodeManager) : SingleNode() {
	override val type: NodeType = NodeType.POWER_EXTRACTOR_NODE

	constructor(network: PowerNodeManager, position: BlockKey) : this(network) {
		this.position = position
		network.extractors[position] = this
	}

	val extractableNodes: MutableSet<PowerInputNode> get() = relationships.mapNotNullTo(mutableSetOf()) { it.value.other as? PowerInputNode }

	// Region transfer
	/*
	 * The extractor node should be allowed to transfer into any regular node.
	 *
	 * Since it does only takes from inputs, it cannot transfer into them.
	 *
	 * And it cannot transfer into any other power source
	 */
	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is PowerInputNode) return false
		return node !is PowerExtractorNode && node !is SolarPanelNode
	}

	private var lastTicked: Long = System.currentTimeMillis()

	fun markTicked() {
		lastTicked = System.currentTimeMillis()
	}

	fun getTransferPower(): Int {
		val interval = IonServer.transportSettings.extractorTickIntervalMS.toDouble()

		return (IonServer.transportSettings.maxPowerRemovedPerExtractorTick * ((System.currentTimeMillis() - lastTicked) / interval)).roundToInt()
	}
	// End region

	// Start region loading
	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	override fun loadIntoNetwork() {
		super.loadIntoNetwork()
		manager.extractors[position] = this
	}

	override fun handlePositionRemoval(position: BlockKey) {
		manager.extractors.remove(position)
		super.handlePositionRemoval(position)
	}

	override fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = manager.getNode(offsetKey) ?: continue

			if (this == neighborNode) return

			if (neighborNode is PowerInputNode) {
				extractableNodes.add(neighborNode)
			}

			// Add a relationship, if one should be added
			addRelationship(position, neighborNode, offset)
		}
	}

	override fun toString(): String {
//		val destinations: Set<PowerInputNode>
//		val time = measureNanoTime {
//			destinations = manager.tickExtractor(this)
//		}
//
//		debugAudience.highlightBlocks(destinations.map { toVec3i(it.position) }, 50L)

		return "Extractor. found"// ${destinations.size} in $time ns"
	}
}

