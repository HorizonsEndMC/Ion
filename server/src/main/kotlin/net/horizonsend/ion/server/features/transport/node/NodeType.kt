package net.horizonsend.ion.server.features.transport.node

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.node.nodes.SpongeNode
import net.horizonsend.ion.server.features.transport.node.nodes.TransportNode

enum class NodeType(val clazz: Class<out TransportNode>) {
	//POWER
	SPONGE_NODE(SpongeNode::class.java),

	//GAS

	//ITEM
	;

	fun newInstance(): TransportNode {
		return clazz.getDeclaredConstructor().newInstance()
	}

	companion object {
		val type = EnumDataType(NodeType::class.java)
		private val byNode: Map<Class<out TransportNode>, NodeType> = entries.associateBy { it.clazz }

		operator fun get(node: TransportNode): NodeType = byNode[node.javaClass] ?: throw NoSuchElementException("Unregistered node type ${node.javaClass.simpleName}!")
	}
}
