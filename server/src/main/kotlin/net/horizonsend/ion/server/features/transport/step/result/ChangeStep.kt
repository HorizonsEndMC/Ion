package net.horizonsend.ion.server.features.transport.step.result

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder
import net.horizonsend.ion.server.features.transport.step.head.StepHead

class ChangeHead<T: ChunkTransportNetwork>(val new: StepHead<T>) : StepResult<T> {
	override fun apply(head: HeadHolder<T>) {
		head.head = new
	}
}
