package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode

interface SingleHead<T: ChunkTransportNetwork> : StepHead<T> {
	var head: TransportNode
}
