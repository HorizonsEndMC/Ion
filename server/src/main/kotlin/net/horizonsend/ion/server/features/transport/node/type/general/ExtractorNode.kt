package net.horizonsend.ion.server.features.transport.node.type.general

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import kotlin.math.roundToInt

abstract class ExtractorNode : SingleNode() {
	private var lastTicked: Long = System.currentTimeMillis()

	fun markTicked() {
		lastTicked = System.currentTimeMillis()
	}

	fun getTransferAmount(): Int {
		val interval = IonServer.transportSettings.extractorTickIntervalMS.toDouble()

		return (IonServer.transportSettings.maxPowerRemovedPerExtractorTick * ((System.currentTimeMillis() - lastTicked) / interval)).roundToInt()
	}

	override fun handlePositionRemoval(position: BlockKey) {
		manager.extractors.remove(position)
		super.handlePositionRemoval(position)
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 0
	}
}
