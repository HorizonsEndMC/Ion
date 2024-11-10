package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.transport.cache.getPowerInputs
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.general.ExtractorNode
import net.horizonsend.ion.server.features.transport.node.type.general.UnTransferableNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience

class PowerExtractorNode(override val manager: PowerNodeManager) : ExtractorNode(), PowerPathfindingNode, UnTransferableNode {
	override val type: NodeType = NodeType.POWER_EXTRACTOR_NODE

	var tickNumber: Int = 0
	var tickInterval: Int = 1

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

	fun getSourcePool() = relationHolder.getAllOthers().mapNotNull { it.other as? PowerInputNode }.flatMap { it.getPoweredEntities() }

	override fun toString(): String {
		val destinations = getPowerInputs(this)
		debugAudience.highlightBlocks(destinations.map { it.getCenter() }, 30L)
		return "Extractor. found, can trasnsfer: ${getTransferAmount()}, numDestinations ${destinations.size}"
	}

	override fun getNextNodes(previous: TransportNode, destination: TransportNode?): ArrayDeque<TransportNode> = cachedTransferable

	override fun loadIntoNetwork() {
		super.loadIntoNetwork()
		manager.extractors[position] = this
	}

	override fun onPlace(position: BlockKey) {
		super.onPlace(position)
		manager.extractors[position] = this
	}
}

