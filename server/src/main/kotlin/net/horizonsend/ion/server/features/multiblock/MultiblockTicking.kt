package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
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
		for ((key, syncTicking) in manager.syncTickingMultiblockEntities) runCatching {
			checkStructureAsyncThenTick(syncTicking)
		}.onFailure { e ->
			log.warn("Exception ticking multiblock ${syncTicking.javaClass.simpleName} at ${toVec3i(key)}: ${e.message}")
			e.printStackTrace()
		}
	}

	private fun checkStructureAsyncThenTick(entity: SyncTickingMultiblockEntity) = Tasks.async {
		if (SyncTickingMultiblockEntity.preTick(entity as MultiblockEntity)) Tasks.sync {
			entity.tick()
		}
	}

	private fun tickAsyncMultiblocks() = iterateManagers { manager ->
		for ((key, asyncTicking) in manager.asyncTickingMultiblockEntities) runCatching {
			if (SyncTickingMultiblockEntity.preTick(asyncTicking as MultiblockEntity)) asyncTicking.tickAsync()
		}.onFailure { e ->
			log.warn("Exception ticking async multiblock ${asyncTicking.javaClass.simpleName} at ${toVec3i(key)}: ${e.message}")
			e.printStackTrace()
		}
	}

	fun iterateManagers(task: (MultiblockManager) -> Unit) {
		@Suppress("UNCHECKED_CAST")
		val clone = managers.clone() as ArrayList<MultiblockManager>

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
