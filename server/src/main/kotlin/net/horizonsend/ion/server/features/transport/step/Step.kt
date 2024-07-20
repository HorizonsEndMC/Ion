package net.horizonsend.ion.server.features.transport.step

import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

class Step<T: TransportNetwork> private constructor(
	val network: T,
	private val stepOrigin: StepOrigin<T>,
) : HeadHolder<T> {
	override lateinit var head: BranchHead<T>

	constructor(network: T, origin: StepOrigin<T>, getHead: Step<T>.() -> BranchHead<T>) : this(network, origin) {
		this.head = getHead()
	}

	constructor(network: T, origin: StepOrigin<T>, head: BranchHead<T>) : this(network, origin) {
		this.head = head
	}

	suspend operator fun invoke() {
		while (!head.isDead()) {
			head.stepForward()
		}
	}

	override fun getOrigin(): StepOrigin<T> {
		return stepOrigin
	}
}
