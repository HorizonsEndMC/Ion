package net.horizonsend.ion.server.features.transport.grid.node

/**
 * A node that may be consolidated.
 **/
interface Consolidatable {
	/**
	 * Consolidates this node if possible
	 **/
	fun consolidate()
}
