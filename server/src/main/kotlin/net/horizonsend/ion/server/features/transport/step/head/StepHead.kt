package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.step.new.NewStep

interface StepHead<T: ChunkTransportNetwork> {
	val holder: HeadHolder<T>
	val parent: NewStep<T>
	var currentNode: TransportNode

	/** Nodes that this head has covered */
	val coveredNodes: MutableSet<TransportNode>

	/**
	 * Moves this step forward
	 * If the head is to be replaced with a new one, return that.
	 **/
	suspend fun stepForward()

	/** Returns whether this head is dead */
	fun isDead(): Boolean
}
