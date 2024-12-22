package net.horizonsend.ion.server.features.world.generation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.place
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.store
import net.horizonsend.ion.server.features.world.generation.generators.interfaces.WorldGenerator
import net.horizonsend.ion.server.features.world.generation.generators.space.GenerateChunk
import net.horizonsend.ion.server.features.world.generation.generators.space.SpaceGenerator
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.cbukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.WorldInitEvent
import kotlin.collections.set

object WorldGenerationManager : SLEventListener() {
	val worldGenerators: MutableMap<World, WorldGenerator?> = mutableMapOf()

	val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	operator fun get(world: World): WorldGenerator? = worldGenerators[world]

	@EventHandler
	fun onWorldInit(event: WorldInitEvent) {
		val serverLevel = event.world

		// TODO
		ConfigurationFiles.serverConfiguration().spaceGenConfig[event.world.name]?.let { config ->
			log.info("Creating generator for ${serverLevel.name}")
			worldGenerators[serverLevel] =
				SpaceGenerator(
					serverLevel,
					config
				)
		}
	}

	@EventHandler
	fun onChunkLoadEvent(event: ChunkLoadEvent) = coroutineScope.launch {
		val generator = get(event.world) ?: return@launch//@runBlocking

		generator.generateChunk(event.chunk)
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
