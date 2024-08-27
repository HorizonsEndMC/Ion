package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

/**
 * A multiblock that handles the processing of something
 *
 * @param requiredProgress The number of ticks to finish this process
 * @param currentProgress The current progress of the process
 **/
abstract class ProcessingMultiblockEntity(
    manager: ChunkMultiblockManager,
    type: Multiblock,
    x: Int,
    y: Int,
    z: Int,
    world: World,
    signOffset: BlockFace,
    val requiredProgress: Int,
    var currentProgress: Int = 0
) : MultiblockEntity(manager, type, x, y, z, world, signOffset), SyncTickingMultiblockEntity {
	//TODO
	// -recipe system

	abstract fun canProcess(): Boolean
	abstract fun finishProcessing()
	abstract fun startProcessing()
	abstract fun process()

	override fun tick() {
		if (currentProgress >= requiredProgress) {
			currentProgress = 0
			finishProcessing()
			return
		}

		currentProgress++
	}

	override fun storeAdditionalData(store: PersistentMultiblockData) {
		store.addAdditionalData(NamespacedKeys.PROCESSING_PROGRESS, PersistentDataType.INTEGER, currentProgress)
	}
}
