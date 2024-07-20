package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

/**
 * Representing the start of a power system
 *
 * Nodes may not transfer into a source node
 **/
interface SourceNode<T: TransportNetwork> : StepHandler<T> {
	suspend fun getOriginData(): StepOrigin<T>?

	suspend fun startStep(): Step<T>?
}
