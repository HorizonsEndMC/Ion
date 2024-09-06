package net.horizonsend.ion.server.features.multiblock.entity.type.ticked

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

/**
 * Any multiblock that should be ticked along with the world
 **/
interface SyncTickingMultiblockEntity : TickedMultiblockEntityParent {
	/**
	 * The logic that is run upon world tick
	 **/
	fun tick()

	companion object {
		/**
		 * returns whether this entity is able / ready to tick.
		 **/
		fun <T: MultiblockEntity> preTick(multiblockEntity: T): Boolean {
			if (multiblockEntity !is TickedMultiblockEntityParent) return false
			if (multiblockEntity.shouldCheckIntegrity() && !multiblockEntity.isIntact(false)) return false

			return multiblockEntity.checkTickInterval()
		}
	}
}
