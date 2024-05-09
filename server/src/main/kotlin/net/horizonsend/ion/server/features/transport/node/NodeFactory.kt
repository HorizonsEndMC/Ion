package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

abstract class NodeFactory<T: ChunkTransportNetwork>(val network: T) {
	/**
	 * Create and handle placement of a node at the position, if one should be created
	 **/
	abstract suspend fun create(key: BlockKey, snapshot: BlockSnapshot)
}
