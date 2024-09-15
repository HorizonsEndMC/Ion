package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.general.LinearNode
import net.horizonsend.ion.server.features.transport.node.manager.node.PowerNodeManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.Axis
import org.bukkit.block.data.Directional

class EndRodNode(network: PowerNodeManager) : LinearNode<PowerNodeManager, EndRodNode, EndRodNode>(network, ) {
	override val type: NodeType = NodeType.END_ROD_NODE
	constructor(network: PowerNodeManager, origin: Long, axis: Axis) : this(network) {
		positions.add(origin)
		this.axis = axis
	}

	override fun addBack(position: BlockKey) {
		val data = getBlockDataSafe(manager.world, getX(position), getY(position), getZ(position)) as? Directional ?: return

		manager.nodeFactory.addEndRod(
			data,
			position,
			handleRelationships = false
		)
	}
}
