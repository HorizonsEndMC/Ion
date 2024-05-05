package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

abstract class NodeFactory<out T: ChunkTransportNetwork> {
	/**
	 * Create and handle placement of a node at the position, if one should be created
	 **/
	abstract suspend fun create(network: @UnsafeVariance T, key: BlockKey, snapshot: BlockSnapshot)
}
