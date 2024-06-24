package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.StepHead

interface DestinationNode<T: ChunkTransportNetwork> {
	suspend fun finishChain(head: StepHead<T>)
}
