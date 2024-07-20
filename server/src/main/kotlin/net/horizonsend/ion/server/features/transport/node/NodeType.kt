package net.horizonsend.ion.server.features.transport.node

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.power.EndRodNode
import net.horizonsend.ion.server.features.transport.node.power.InvertedDirectionalNode
import net.horizonsend.ion.server.features.transport.node.power.PowerDirectionalNode
import net.horizonsend.ion.server.features.transport.node.power.PowerEqualSplitterNode
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.power.SpongeNode

enum class NodeType(val clazz: Class<out TransportNode>) {
	//POWER
	SPONGE_NODE(SpongeNode::class.java),
	END_ROD_NODE(EndRodNode::class.java),
	SOLAR_PANEL_NODE(SolarPanelNode::class.java),
	POWER_EXTRACTOR_NODE(PowerExtractorNode::class.java),
	POWER_INPUT_NODE(PowerInputNode::class.java),
	POWER_FLOW_METER(PowerFlowMeter::class.java),
	POWER_DIRECTIONAL_NODE(PowerDirectionalNode::class.java),
	POWER_INVERSE_DIRECTIONAL_NODE(InvertedDirectionalNode::class.java),
	POWER_EQUAL_SPLITTER(PowerEqualSplitterNode::class.java),

	//GAS

	//ITEM
	;

	fun newInstance(network: ChunkTransportNetwork): TransportNode {
		return clazz.getDeclaredConstructor(network::class.java).newInstance(network)
	}

	fun newInstance(): TransportNode {
		return clazz.getDeclaredConstructor().newInstance()
	}

	companion object {
		val type = EnumDataType(NodeType::class.java)
		private val byNode: Map<Class<out TransportNode>, NodeType> = entries.associateBy { it.clazz }

		operator fun get(node: TransportNode): NodeType = byNode[node.javaClass] ?: throw NoSuchElementException("Unregistered node type ${node.javaClass.simpleName}!")
	}
}
