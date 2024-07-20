package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

interface HeadHolder<T: TransportNetwork> {
	var head: BranchHead<T>

	fun getOrigin(): StepOrigin<T>
}
