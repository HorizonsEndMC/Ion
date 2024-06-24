package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.StepHead
import net.horizonsend.ion.server.features.transport.step.result.StepResult

interface IntermediateNode<T: ChunkTransportNetwork> {
	/**
	 * Handle the stepping of power through this node
	 *
	 * This may create a new step for a single node, spawn off multiple steps, or more
	 * Each node defines how it is stepped.
	 **/
	suspend fun handleHeadStep(head: StepHead<T>): StepResult<T>
}
