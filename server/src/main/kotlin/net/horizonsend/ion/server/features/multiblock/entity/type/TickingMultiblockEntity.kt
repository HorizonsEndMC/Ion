package net.horizonsend.ion.server.features.multiblock.entity.type

/**
 * Any multiblock that should be ticked along with the world
 **/
interface TickingMultiblockEntity {
	/**
	 * Whether the tick should be run async
	 **/
	val tickAsync: Boolean

	/**
	 * The logic that is run upon world tick
	 **/
	suspend fun tick()
}
