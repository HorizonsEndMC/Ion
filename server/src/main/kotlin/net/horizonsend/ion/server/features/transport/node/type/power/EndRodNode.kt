package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.general.LinearNode
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.Axis
import org.bukkit.block.data.Directional

class EndRodNode(network: PowerNodeManager) : LinearNode<PowerNodeManager, EndRodNode, EndRodNode>(network) {
	override val type: NodeType = NodeType.END_ROD_NODE

	constructor(network: PowerNodeManager, origin: Long, axis: Axis) : this(network) {
		positions.add(origin)
		this.axis = axis
	}

	override fun addBack(position: BlockKey) {
		val data = getBlockDataSafe(manager.world, getX(position), getY(position), getZ(position)) as? Directional ?: return

		manager.nodeFactory.addLinearNode<EndRodNode>(position, data.facing.axis, type, handleRelationships = false)
	}
}
