package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.general.FlowMeter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.YELLOW

class PowerFlowMeter(override val manager: PowerNodeManager) : FlowMeter(), PowerPathfindingNode {
	override val type: NodeType = NodeType.POWER_FLOW_METER

	/*
	 * Should transfer power like any normal node.
	 *
	 * And it cannot transfer into a source
	 */
	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is PowerExtractorNode && node !is SolarPanelNode
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 1
	}

	override fun getNextNodes(previous: TransportNode, destination: TransportNode?): ArrayDeque<TransportNode> = cachedTransferable

	companion object {
		val firstLine = text("E: ", YELLOW)
		val secondLine = ofChildren(newline(), text("E ", YELLOW), text("/ ", HE_MEDIUM_GRAY), text("Second", GREEN))
	}

	override fun formatFlow(): Component {
		var avg = runCatching { calculateAverage().roundToHundredth() }.getOrDefault(0.0)

		// If no averages, or no power has been moved in 5 seconds, go to 0
		if (averages.isEmpty() || System.currentTimeMillis() - averages.maxOf { it.time } > 5000) {
			avg = 0.0
		}

		return ofChildren(firstLine, text(avg, GREEN), secondLine)
	}
}
