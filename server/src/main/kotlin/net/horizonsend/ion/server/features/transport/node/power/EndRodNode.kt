package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.node.general.LinearNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Axis
import org.bukkit.block.data.Directional

class EndRodNode(network: PowerNodeManager) : LinearNode<PowerNodeManager, EndRodNode, EndRodNode>(network, ) {
	constructor(network: PowerNodeManager, origin: Long, axis: Axis) : this(network) {
		positions.add(origin)
		this.axis = axis
	}

	override suspend fun addBack(position: BlockKey) {
		manager.nodeFactory.addEndRod(
			getBlockSnapshotAsync(manager.world, position)!!.data as Directional,
			position,
			handleRelationships = false
		)
	}
}
