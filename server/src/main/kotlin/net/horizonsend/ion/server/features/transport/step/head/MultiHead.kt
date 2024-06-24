package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork

/** A head which branches into multiple sub-heads */
interface MultiHead<N: ChunkTransportNetwork> : StepHead<N> {
	val heads: MutableSet<MultiHeadHolder<N>>

	/** Holder that allows the head within to be replaced */
	class MultiHeadHolder<N: ChunkTransportNetwork>(override var head: StepHead<N>) : HeadHolder<N>

	// Just pass it forward
	override suspend fun stepForward() {
		heads.forEach { it.head.stepForward() }
	}

	// Just pass it forward
	override fun isDead(): Boolean {
		return heads.all { it.head.isDead() }
	}
}
