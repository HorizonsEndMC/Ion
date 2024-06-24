package net.horizonsend.ion.server.features.transport.step.result

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder

class MoveForward<T: ChunkTransportNetwork> : StepResult<T> {
	override fun apply(head: HeadHolder<T>) {
		val currentNode = head.head.currentNode
	}
}
