package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode

interface BranchHead<T: TransportNetwork> {
	val holder: HeadHolder<T>

	/** Nodes that this head has covered */
	val previousNodes: MutableSet<TransportNode>

	/**
	 * Moves this step forward
	 * If the head is to be replaced with a new one, return that.
	 **/
	suspend fun stepForward()

	/** Returns whether this head is dead */
	fun isDead(): Boolean

	/** Sets this branch to be dead */
	fun markDead()
}