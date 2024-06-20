package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

/**
 * Any multiblock that should be ticked along with the world
 **/
interface SyncTickingMultiblockEntity {
	/**
	 * The logic that is run upon world tick
	 **/
	fun tick()

	/**
	 *
	 **/
	fun shouldTick(): Boolean = true

	/**
	 *
	 **/
	fun shouldCheckIntegrity(): Boolean = true

	companion object {
		/**
		 * Checks whether this entity is able / ready to tick.
		 **/
		fun <T: MultiblockEntity> preTick(multiblockEntity: T): Boolean {
			if (multiblockEntity !is SyncTickingMultiblockEntity) return false
			if (multiblockEntity.shouldCheckIntegrity() && !multiblockEntity.isIntact()) return false

			return multiblockEntity.shouldTick()
		}
	}
}
