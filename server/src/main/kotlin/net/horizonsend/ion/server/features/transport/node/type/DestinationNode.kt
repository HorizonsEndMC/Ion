package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.BranchHead

interface DestinationNode<T: TransportNetwork> {
	suspend fun finishChain(head: BranchHead<T>)
}
