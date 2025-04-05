package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object MultiblockTicking : IonServerComponent() {
	private val managers = ArrayList<MultiblockManager>()

	private lateinit var executor: ExecutorService

	override fun onEnable() {
		executor = Executors.newCachedThreadPool(Tasks.namedThreadFactory("asnc-multiblock-worker"))

		Tasks.asyncRepeat(1L, 1L, ::tickAsyncMultiblocks)
		Tasks.syncRepeat(1L, 1L, ::tickSyncMultiblocks)
	}

	override fun onDisable() {
		if (::executor.isInitialized) executor.shutdown()
	}

	private fun tickSyncMultiblocks() = iterateManagers { manager ->
		var multiblock: SyncTickingMultiblockEntity? = null

		for (key in manager.syncTickingMultiblockEntities.keys) runCatching {
			val syncTicking = manager.syncTickingMultiblockEntities[key] ?: return@runCatching
			multiblock = syncTicking
			checkStructureAsyncThenTick(syncTicking)
		}.onFailure { e ->
			log.warn("Exception ticking multiblock ${multiblock?.javaClass?.simpleName} at ${toVec3i(key)}: ${e.message}")
			e.printStackTrace()
		}
	}

	private fun checkStructureAsyncThenTick(entity: SyncTickingMultiblockEntity) = Tasks.async {
		if (SyncTickingMultiblockEntity.preTick(entity as MultiblockEntity)) Tasks.sync {
			entity.tick()
		}
	}

	private fun tickAsyncMultiblocks() = iterateManagers { manager ->
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

	inline fun iterateManagers(task: (MultiblockManager) -> Unit) {
		@Suppress("UNCHECKED_CAST")
		val clone = getAllMultiblockManagers().clone() as ArrayList<MultiblockManager>

		clone.forEach(task)
	}

	fun registerMultiblockManager(manager: MultiblockManager) {
		managers.add(manager)
	}

	fun removeMultiblockManager(manager: MultiblockManager) {
		managers.remove(manager)
	}

	fun getAllMultiblockManagers() = managers
}
