package net.horizonsend.ion.server.features.world.generation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.place
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.store
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.generation.generators.space.GenerateChunk
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.cbukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.world.ChunkLoadEvent
import java.util.concurrent.Executors

object WorldGenerationManager : IonServerComponent() {
	val internalThread = Executors.newFixedThreadPool(16, Tasks.namedThreadFactory("worldgen"))
	val thread = internalThread.asCoroutineDispatcher()
	val coroutineScope = CoroutineScope(thread + SupervisorJob())

	@EventHandler
	fun onChunkLoadEvent(event: ChunkLoadEvent) {
		val generator = event.chunk.world.ion.terrainGenerator ?: return
		return
		coroutineScope.launch {
			generator.generateChunk(event.chunk)
		}
	}

	override fun onDisable() {
		internalThread.shutdown()
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	suspend fun handleGeneration(task: GenerateChunk, scope: CoroutineScope) {
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
