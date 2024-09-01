package net.horizonsend.ion.server.features.transport.node.gas

import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.network.FluidNetwork
import net.horizonsend.ion.server.features.transport.node.general.LinearNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Axis
import org.bukkit.block.data.Directional

class LightningRodNode(network: FluidNetwork) : LinearNode<FluidNetwork, LightningRodNode, LightningRodNode>(network) {
	constructor(network: FluidNetwork, origin: Long, axis: Axis) : this(network) {
		positions.add(origin)
		this.axis = axis
	}

	override suspend fun addBack(position: BlockKey) {
		network.nodeFactory.addLightningRod(
			getBlockSnapshotAsync(network.world, position)!!.data as Directional,
			position,
			handleRelationships = false
		)
	}
}
