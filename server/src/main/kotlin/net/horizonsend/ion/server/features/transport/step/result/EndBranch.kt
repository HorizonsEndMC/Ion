package net.horizonsend.ion.server.features.transport.step.result

import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder

/** A result which marks the branch as dead */
class EndBranch<T: TransportNetwork> : StepResult<T> {
	override suspend fun apply(headHolder: HeadHolder<T>) {
		headHolder.head.markDead()
	}
}
