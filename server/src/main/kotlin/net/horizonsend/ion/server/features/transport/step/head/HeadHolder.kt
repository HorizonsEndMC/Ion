package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

interface HeadHolder<T: ChunkTransportNetwork> {
	var head: BranchHead<T>

	fun getOrigin(): StepOrigin<T>
}
