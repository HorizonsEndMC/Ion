package net.horizonsend.ion.server.features.multiblock.type

/**
 * Any multiblock that should be ticked along with the world
 **/
interface TickingMultiblock {
	/**
	 * Whether the tick should be run async
	 **/
	val tickAsync: Boolean

	/**
	 * The logic that is run upon world tick
	 **/
	fun tick()
}
