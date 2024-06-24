package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork

interface HeadHolder<T: ChunkTransportNetwork> {
	var head: StepHead<T>
}
