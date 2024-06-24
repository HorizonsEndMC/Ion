package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode

interface SingleBranchHead<T: ChunkTransportNetwork> : BranchHead<T> {
	var currentNode: TransportNode
}
