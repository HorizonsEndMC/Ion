package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.new.NewStep
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

/**
 * Representing the start of a power system
 *
 * Nodes may not transfer into a source node
 **/
interface SourceNode<T: ChunkTransportNetwork> {
	suspend fun getOriginData(): StepOrigin<T>

	suspend fun startStep(): NewStep<T>?
}
