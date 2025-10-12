package net.horizonsend.ion.server.features.custom.blocks.misc

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.util.getBlockEntity
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.minecraft.world.level.block.entity.CommandBlockEntity
import org.bukkit.World
import org.bukkit.persistence.PersistentDataType

interface GaugeCustomBlock {
	/**
	 * Sets the redstone output of this gauge, returns whether the signal could be set.
	 **/
	fun setSignalOutput(value: Int, world: World, blockLocation: Vec3i): Boolean

	fun isMultiblockOwned(multiblockManager: MultiblockManager, blockLocation: Vec3i): Boolean

	fun setMultiblockOwner(multiblockManager: MultiblockManager, blockLocation: Vec3i, multiblock: MultiblockEntity)

	object CommandBlockGaugeCustomBlock : GaugeCustomBlock {
		override fun isMultiblockOwned(multiblockManager: MultiblockManager, globalBlockLocation: Vec3i): Boolean {
			val commandBlockEntity = getBlockEntity(globalBlockLocation, multiblockManager.world) as? CommandBlockEntity ?: return false

			val localKey = commandBlockEntity.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK_OWNER, PersistentDataType.LONG) ?: return false
			val localOffset = toVec3i(localKey)
			val entity = multiblockManager[localOffset.x, localOffset.y, localOffset.z]
			return entity != null && !entity.removed
		}

		override fun setMultiblockOwner(multiblockManager: MultiblockManager, blockLocation: Vec3i, multiblock: MultiblockEntity) {
			val globalCoordinate = multiblockManager.getGlobalCoordinate(blockLocation)
			val commandBlockEntity = getBlockEntity(globalCoordinate, multiblockManager.world) as? CommandBlockEntity ?: return

			val entityKey = toBlockKey(multiblock.localVec3i)
			commandBlockEntity.persistentDataContainer.set(NamespacedKeys.MULTIBLOCK_OWNER, PersistentDataType.LONG, entityKey)
		}

		override fun setSignalOutput(value: Int, world: World, blockLocation: Vec3i): Boolean {
			val entity = getBlockEntity(blockLocation, world) as? CommandBlockEntity ?: return false

			Tasks.sync {
				entity.commandBlock.successCount = value
				entity.level?.updateNeighbourForOutputSignal(entity.blockPos, entity.blockState.block)
			}

			return true
		}
	}
}
