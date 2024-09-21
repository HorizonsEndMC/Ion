package net.horizonsend.ion.server.features.transport.node

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.features.transport.node.type.fluid.FluidLinearNode
import net.horizonsend.ion.server.features.transport.node.type.power.EndRodNode
import net.horizonsend.ion.server.features.transport.node.type.power.InvertedDirectionalNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerDirectionalNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.node.type.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.type.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.type.power.SpongeNode

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

	//FLUID
	FLUID_INPUT(net.horizonsend.ion.server.features.transport.node.type.fluid.FluidInputNode::class.java),
	FLUID_JUNCTION(net.horizonsend.ion.server.features.transport.node.type.fluid.FluidJunctionNode::class.java),
	LIGHTNING_ROD(FluidLinearNode::class.java),

	//ITEM
	;

	fun newInstance(network: NodeManager): TransportNode {
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
