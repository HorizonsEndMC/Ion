package net.horizonsend.ion.server.features.multiblock.entity.type

/**
 * Any multiblock that should be ticked along with the world
 **/
interface AsyncTickingMultiblockEntity {
	/**
	 * The logic that is run upon world tick
	 **/
	suspend fun tickAsync()

	/**
	 *
	 **/
	fun shouldAsyncTick(): Boolean = true

	/**
	 *
	 **/
	fun shouldAsyncCheckIntegrity(): Boolean = true
}
