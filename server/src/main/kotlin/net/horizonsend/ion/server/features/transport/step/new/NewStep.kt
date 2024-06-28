package net.horizonsend.ion.server.features.transport.step.new

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.head.HeadHolder
import net.horizonsend.ion.server.features.transport.step.head.power.SinglePowerBranchHead
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

class NewStep<T: ChunkTransportNetwork>(
	val network: T,
	val origin: StepOrigin<T>,
) : HeadHolder<T> {
	override lateinit var head: BranchHead<T>

	constructor(network: T, origin: StepOrigin<T>, getHead: NewStep<T>.() -> BranchHead<T>) : this(network, origin) {
		this.head = getHead()
	}

	constructor(network: T, origin: StepOrigin<T>, head: BranchHead<T>) : this(network, origin) {
		this.head = head
	}

	suspend operator fun invoke() {
		while (!head.isDead()) {
			println("Stepping forward. Head: $head")
			println("Current head position: ${(head as? SinglePowerBranchHead)?.currentNode}")
			println("Previous nodes: ${head.previousNodes}")
			head.stepForward()
		}
	}
}
