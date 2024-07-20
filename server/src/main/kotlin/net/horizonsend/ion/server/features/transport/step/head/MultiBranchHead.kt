package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin
import kotlin.properties.Delegates

/** A head which branches into multiple sub-heads */
interface MultiBranchHead<N: TransportNetwork> : BranchHead<N> {
	val heads: MutableSet<MultiHeadHolder<N>>

	/** Holder that allows the head within to be replaced */
	class MultiHeadHolder<N: TransportNetwork>() : HeadHolder<N> {
		override var head: BranchHead<N> by Delegates.notNull()

		constructor(head: BranchHead<N>) : this() {
			this.head = head
		}

		constructor(constructHead: (MultiHeadHolder<N>) -> BranchHead<N>) : this() {
			this.head = constructHead.invoke(this)
		}

		override fun getOrigin(): StepOrigin<N> {
			return head.holder.getOrigin()
		}
	}

	// Just pass it forward
	override suspend fun stepForward() {
		heads.forEach { it.head.stepForward() }
	}

	// Just pass it forward
	override fun isDead(): Boolean {
		return heads.all { it.head.isDead() }
	}

	override fun markDead() {
		heads.forEach { it.head.markDead() }
	}
}
