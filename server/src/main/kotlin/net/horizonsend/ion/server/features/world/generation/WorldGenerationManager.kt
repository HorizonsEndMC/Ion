package net.horizonsend.ion.server.features.world.generation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.place
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.store
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.generation.generators.space.GenerateChunk
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.cbukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.world.ChunkLoadEvent

object WorldGenerationManager : SLEventListener() {
	val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	@EventHandler
	fun onChunkLoadEvent(event: ChunkLoadEvent) {
		val generator = event.chunk.world.ion.terrainGenerator ?: return
		coroutineScope.launch {
			generator.generateChunk(event.chunk)
		}
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
