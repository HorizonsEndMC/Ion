package net.horizonsend.ion.server.features.transport.node.fluid

import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.grid.GridType
import net.horizonsend.ion.server.features.transport.node.general.LinearNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Axis
import org.bukkit.block.data.Directional

class LightningRodNode(network: FluidNodeManager) : LinearNode<FluidNodeManager, LightningRodNode, LightningRodNode>(network, GridType.Fluid) {
	constructor(network: FluidNodeManager, origin: Long, axis: Axis) : this(network) {
		positions.add(origin)
		this.axis = axis
	}

	override suspend fun addBack(position: BlockKey) {
		manager.nodeFactory.addLightningRod(
			getBlockSnapshotAsync(manager.world, position)!!.data as Directional,
			position,
			handleRelationships = false
		)
	}
}
