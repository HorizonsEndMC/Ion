package net.horizonsend.ion.server.features.transport.step.new

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder
import net.horizonsend.ion.server.features.transport.step.head.StepHead
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

class NewStep<T: ChunkTransportNetwork>(
	val network: T,
	val origin: StepOrigin<T>,
	override var head: StepHead<T>,
) : HeadHolder<T> {
	suspend operator fun invoke() {
		while (!head.isDead()) {
			head.stepForward()
		}
	}
}
