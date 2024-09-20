package net.horizonsend.ion.server.features.multiblock.entity.type.ticked

/**
 * Any multiblock that should be ticked along with the world
 **/
interface AsyncTickingMultiblockEntity : TickedMultiblockEntityParent {
	/**
	 * The logic that is run upon world tick
	 **/
	fun tickAsync()
}
