package net.horizonsend.ion.server.features.transport.node.fluid

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.general.LinearNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.Axis
import org.bukkit.block.data.Directional

class LightningRodNode(network: FluidNodeManager) : LinearNode<FluidNodeManager, LightningRodNode, LightningRodNode>(network) {
	override val type: NodeType = NodeType.LIGHTNING_ROD

	constructor(network: FluidNodeManager, origin: Long, axis: Axis) : this(network) {
		positions.add(origin)
		this.axis = axis
	}

	override fun addBack(position: BlockKey) {
		val data = getBlockDataSafe(manager.world, getX(position), getY(position), getZ(position)) as? Directional ?: return

		manager.nodeFactory.addLightningRod(
			data,
			position,
			handleRelationships = false
		)
	}
}
