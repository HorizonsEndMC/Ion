package net.horizonsend.ion.server.features.world.chunk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Chunk
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class ChunkRegion(val world: IonWorld) {
	val chunks = ConcurrentHashMap<Long, IonChunk>()

	val size get() = chunks.values.size

	fun removeChunk(chunk: IonChunk) {
		chunks.remove(chunk.locationKey)
	}

	fun delete() {
		chunks.forEach {
			world.regionPositions.remove(it.value.locationKey)
		}
		world.chunkRegions.remove(this)
	}

	fun launch(block: suspend CoroutineScope.() -> Unit) = scope.launch { block.invoke(this) }

	companion object {
		val scope = CoroutineScope(Executors.newCachedThreadPool(Tasks.namedThreadFactory("chunk-async-worker")).asCoroutineDispatcher() + SupervisorJob())
		const val MAX_SIZE: Int = 50

		fun loadChunk(chunk: Chunk) {
			if (chunk.world.ion.getChunk(chunk.chunkKey) != null) {
				return
			}

			val region: ChunkRegion = findRegion(chunk)
			val ionChunk = IonChunk.registerChunk(chunk, region)

			val key = chunk.chunkKey

			region.chunks[key] = ionChunk
			chunk.world.ion.regionPositions[key] = region

			region.world.chunkRegions.add(region)
		}

		fun unloadChunk(chunk: IonChunk) {
			val region = chunk.region

			region.removeChunk(chunk)

			if (region.chunks.isEmpty()) region.delete()
		}

		/**
		 * Will find a region for this chunk to join.
		 *
		 * If this chunk borders a region, and it can add another safely, join it.
		 *
		 * If there are two disconnected regions joined by this chunk, and they total less than the limit, combine them.
		 *
		 * If there is no region available, start a new one.
		 *
		 * Returns the region
		 **/
		fun findRegion(chunk: Chunk): ChunkRegion {
			val neighbors = getNeighborRegions(chunk)

			val region: ChunkRegion = when (neighbors.size) {
				0 -> startNewRegion(chunk)
				1 -> {
					val region = neighbors.first()
					if (region.size > MAX_SIZE) startNewRegion(chunk) else region
				}
				in 2..4 -> {
					neighbors.firstOrNull { it.size < MAX_SIZE } ?: startNewRegion(chunk)
				}
				else -> throw IllegalArgumentException()
			}

			return region
		}

		private fun getNeighborRegions(chunk: Chunk): Collection<ChunkRegion> {
			val chunkX = chunk.x
			val chunkZ = chunk.z

			return CARDINAL_BLOCK_FACES.mapNotNull { direction ->
				val newX = direction.modX + chunkX
				val newZ = direction.modZ + chunkZ

				val key = Chunk.getChunkKey(newX, newZ)
				chunk.world.ion.regionPositions[key]
			}
		}

		fun startNewRegion(chunk: Chunk): ChunkRegion {
			val world = chunk.world.ion
			val region =  ChunkRegion(world)
			world.chunkRegions.add(region)

			return region
		}
	}

	override fun toString(): String {
		return "IonChunkRegion[${size} positions]"
	}
}

