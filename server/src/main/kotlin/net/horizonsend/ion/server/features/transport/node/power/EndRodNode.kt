package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.node.general.LinearNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Axis
import org.bukkit.block.data.Directional

class EndRodNode(network: PowerNetwork) : LinearNode<PowerNetwork, EndRodNode, EndRodNode>(network) {
	constructor(network: PowerNetwork, origin: Long, axis: Axis) : this(network) {
		positions.add(origin)
		this.axis = axis
	}

	override suspend fun addBack(position: BlockKey) {
		network.nodeFactory.addEndRod(
			getBlockSnapshotAsync(network.world, position)!!.data as Directional,
			position,
			handleRelationships = false
		)
	}
}
