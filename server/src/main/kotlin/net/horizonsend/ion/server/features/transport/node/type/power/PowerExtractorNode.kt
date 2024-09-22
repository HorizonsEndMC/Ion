package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.math.roundToInt

class PowerExtractorNode(override val manager: PowerNodeManager) : SingleNode() {
	override val type: NodeType = NodeType.POWER_EXTRACTOR_NODE

	constructor(network: PowerNodeManager, position: BlockKey) : this(network) {
		this.position = position
		network.extractors[position] = this
	}

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

	override fun onPlace(position: BlockKey) {
		super.onPlace(position)
		manager.extractors[position] = this
	}

	override fun handlePositionRemoval(position: BlockKey) {
		manager.extractors.remove(position)
		super.handlePositionRemoval(position)
	}

	/**
	 * Attempts to draw power from connected inputs.
	 * Returns the amount that couldn't be removed.
	 **/
	fun drawPower(amount: Int): Int {
		val entities = relationships.mapNotNull { (it.value.other as? PowerInputNode)?.getPoweredEntities()?.randomOrNull() } //TODO

		var remaining = amount

		while (remaining > 0) {
			val available = entities.filterNot { it.storage.isEmpty() }
			if (available.isEmpty()) break

			val minPower = entities.minOf { it.storage.getPower() }
			val idealShare = remaining / available.size
			val toRemove = minOf(idealShare, minPower)

			available.forEach {
				val r = it.storage.removePower(toRemove)
				remaining -= (toRemove - r)
			}
		}

		return remaining
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 0
	}

	fun getSourcePool() = relationships.mapNotNull { it.value.other as? PowerInputNode }.flatMap { it.getPoweredEntities() }

	override fun toString(): String {
		return "Extractor. found, can trasnsfer: ${getTransferPower()}"
	}
}

