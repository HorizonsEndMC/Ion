package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i

object MultiblockTicking : IonServerComponent() {
	private val managers = ArrayList<MultiblockManager>()
	val count get() = managers.size

	override fun onEnable() {
		Tasks.asyncRepeat(1L, 1L, ::tickAsyncMultiblocks)
		Tasks.asyncRepeat(1L, 1L, ::tickSyncMultiblocks)
	}

	override fun onDisable() {
	}

	private fun tickSyncMultiblocks() {
		if (!IonServer.isEnabled) return

		iterateManagers { manager: MultiblockManager? ->
			if (manager == null) return@iterateManagers

			var multiblock: SyncTickingMultiblockEntity? = null

			for (key in manager.syncTickingMultiblockEntities.keys) runCatching {
				val syncTicking = manager.syncTickingMultiblockEntities[key] ?: return@runCatching
				multiblock = syncTicking

				if (SyncTickingMultiblockEntity.preTick(syncTicking as MultiblockEntity)) Tasks.sync {
					syncTicking.tick()
				}
			}.onFailure { e ->
				log.warn("Exception ticking multiblock ${multiblock?.javaClass?.simpleName} at ${toVec3i(key)}: ${e.message}")
				e.printStackTrace()
			}
		}
	}

	private fun tickAsyncMultiblocks() {
		if (!IonServer.isEnabled) return

		iterateManagers { manager: MultiblockManager? ->
			if (manager == null) return@iterateManagers

			var multiblock: AsyncTickingMultiblockEntity? = null

			for (key in manager.asyncTickingMultiblockEntities.keys) runCatching {
				val asyncTicking = manager.asyncTickingMultiblockEntities[key] ?: return@runCatching
				multiblock = asyncTicking
				if (SyncTickingMultiblockEntity.preTick(asyncTicking as MultiblockEntity)) asyncTicking.tickAsync()
			}.onFailure { e ->
				log.warn("Exception ticking async multiblock ${multiblock?.javaClass?.simpleName} at ${toVec3i(key)}: ${e.message}")
				e.printStackTrace()
			}
		}
	}

	inline fun iterateManagers(task: (MultiblockManager) -> Unit) {
		getAllMultiblockManagers().toArray<MultiblockManager>(arrayOfNulls(count)).forEach(task)
	}

	fun registerMultiblockManager(manager: MultiblockManager) {
		managers.add(manager)
	}

	fun removeMultiblockManager(manager: MultiblockManager) {
		managers.remove(manager)
	}

	fun getAllMultiblockManagers() = managers
}
