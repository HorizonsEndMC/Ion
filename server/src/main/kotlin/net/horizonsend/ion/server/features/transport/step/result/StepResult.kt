package net.horizonsend.ion.server.features.transport.step.result

import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder

interface StepResult<T: TransportNetwork> {
	suspend fun apply(headHolder: HeadHolder<T>)
}

