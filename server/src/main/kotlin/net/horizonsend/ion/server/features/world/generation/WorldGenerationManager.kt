package net.horizonsend.ion.server.features.world.generation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.place
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.store
import net.horizonsend.ion.server.features.world.generation.generators.space.GenerateChunk
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.cbukkit
import java.util.concurrent.Executors

object WorldGenerationManager : IonServerComponent() {
	val internalThread = Executors.newFixedThreadPool(16, Tasks.namedThreadFactory("worldgen"))
	val thread = internalThread.asCoroutineDispatcher()
	val coroutineScope = CoroutineScope(thread + SupervisorJob())

	override fun onDisable() {
		internalThread.shutdown()
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun handleGeneration(task: GenerateChunk, scope: CoroutineScope) {
		task.generateChunk(scope)
		val completableData = task.returnData

		if (task.isCancelled) return

		task.returnData.invokeOnCompletion {
			val completed = completableData.getCompleted()

			Tasks.syncBlocking {
				completed.place(task.chunk.cbukkit)
				completed.store(task.chunk.cbukkit)
			}
		}
	}
}
