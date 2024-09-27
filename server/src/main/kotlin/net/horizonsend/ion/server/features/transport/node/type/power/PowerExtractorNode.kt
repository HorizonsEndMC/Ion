package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.manager.getPowerInputs
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import kotlin.math.roundToInt

class PowerExtractorNode(override val manager: PowerNodeManager) : SingleNode(), PowerPathfindingNode {
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

	private var lastTicked: Long = System.currentTimeMillis()

	fun markTicked() {
		lastTicked = System.currentTimeMillis()
	}

	fun getTransferPower(): Int {
		val interval = IonServer.transportSettings.extractorTickIntervalMS.toDouble()

		return (IonServer.transportSettings.maxPowerRemovedPerExtractorTick * ((System.currentTimeMillis() - lastTicked) / interval)).roundToInt()
	}

	override fun loadIntoNetwork() {
		super.loadIntoNetwork()
		manager.extractors[position] = this
	}

	override fun onPlace(position: BlockKey) {
		super.onPlace(position)
		manager.extractors[position] = this
	}

	override fun handlePositionRemoval(position: BlockKey) {
		manager.extractors.remove(position)
		super.handlePositionRemoval(position)
	}

//	/**
//	 * Attempts to draw power from connected inputs.
//	 * Returns the amount that couldn't be removed.
//	 **/
//	fun drawPower(amount: Int): Int {
//		val entities = mutableListOf<PoweredMultiblockEntity>()
//
//		for (relation in relationships) {
//			val node = relation.value.other
//			if (node !is PowerInputNode) continue
//			entities.addAll(node.getPoweredEntities())
//		}
//
//		/*
//		var remaining = amount
//
//		while (remaining > 0) {
//			val available = entities.filterNot { it.storage.isEmpty() }
//			if (available.isEmpty()) break
//
//			val minPower = entities.minOf { it.storage.getPower() }
//			val idealShare = remaining / available.size
//			val toRemove = minOf(idealShare, minPower)
//
//			available.forEach {
//				val r = it.storage.removePower(toRemove)
//				remaining -= (toRemove - r)
//			}
//		}
//
//		*/
//
//		val entity = entities.randomOrNull() ?: return amount
//		val remaining = entity.storage.removePower(amount)
//
//		return remaining
//	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 0
	}

	fun getSourcePool() = relationHolder.getAllOthers().mapNotNull { it.other as? PowerInputNode }.flatMap { it.getPoweredEntities() }

	override fun toString(): String {
		val destinations = getPowerInputs(this)
		debugAudience.highlightBlocks(destinations.map { it.getCenter() }, 30L)
		return "Extractor. found, can trasnsfer: ${getTransferPower()}, numDestinations ${destinations.size}"
	}

	override fun getNextNodes(previous: TransportNode, destination: TransportNode?): ArrayDeque<TransportNode> = cachedTransferable
}

