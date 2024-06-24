package net.horizonsend.ion.server.features.transport.step.result

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder

/** A result which changes the head of the step to a new one */
class ChangeHead<T: ChunkTransportNetwork>(val new: BranchHead<T>) : StepResult<T> {
	override suspend fun apply(headHolder: HeadHolder<T>) {
		headHolder.head = new
	}
}
