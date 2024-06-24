package net.horizonsend.ion.server.features.transport.step.result

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder

interface StepResult<T: ChunkTransportNetwork> {
	suspend fun apply(headHolder: HeadHolder<T>)
}

