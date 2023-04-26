package net.horizonsend.ion.server.features.space.generation

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.place
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.store
import net.horizonsend.ion.server.features.space.generation.generators.GenerateChunk
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerator
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.SPACE_GEN_VERSION
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.starlegacy.util.Tasks
import net.starlegacy.util.component1
import net.starlegacy.util.component2
import net.starlegacy.util.distance
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.persistence.PersistentDataType.BYTE
import java.util.Random
import kotlin.math.ceil

object SpaceGenerationManager : Listener {
	val worldGenerators: MutableMap<ServerLevel, SpaceGenerator?> = mutableMapOf()

	val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	fun getGenerator(serverLevel: ServerLevel): SpaceGenerator? = worldGenerators[serverLevel]

	@EventHandler
	fun onWorldInit(event: WorldInitEvent) {
		val serverLevel = (event.world as CraftWorld).handle

		IonServer.configuration.spaceGenConfig[event.world.name]?.let { config ->
			IonServer.slF4JLogger.info("Creating generator for ${serverLevel.serverLevelData.levelName}")
			worldGenerators[serverLevel] =
				SpaceGenerator(
					serverLevel,
					config
				)
		}
	}

	@Deprecated("Event Listener")
	@EventHandler
	fun onChunkLoadEvent(event: ChunkLoadEvent) = coroutineScope.launch {
		val generator = getGenerator(event.world.minecraft) ?: return@launch//@runBlocking
		if (event.chunk.persistentDataContainer.has(SPACE_GEN_VERSION)) return@launch//@runBlocking

		event.chunk.persistentDataContainer.set(SPACE_GEN_VERSION, BYTE, generator.spaceGenerationVersion)

		val chunkPos = event.chunk.minecraft.pos

		// Generate a number of random asteroids in a chunk, proportional to the density in a portion of the chunk. Allows densities of X>1 asteroid per chunk.

		// Asteroids
		val cornerX = chunkPos.x.shl(4)
		val cornerZ = chunkPos.z.shl(4)

		val acquiredAsteroids = search(generator, chunkPos) { chunkSeed, chunkRandom, maxHeight, minHeight, x, y, z ->
			val asteroid = generator.generateRandomAsteroid(chunkSeed, chunkRandom, maxHeight, minHeight, x, y, z)
			val distance = distance(cornerX, cornerZ, asteroid.x, asteroid.z)
			if (distance > (asteroid.size * 1.25)) null else asteroid
		}

		val acquiredWrecks = if (generator.configuration.wreckClasses.isNotEmpty()) {
			search(generator, chunkPos, 4207097) { _, chunkRandom, _, _, x, y, z ->
				generator.generateRandomWreckData(chunkRandom, x, y, z)
			}
		} else listOf()

		if (acquiredAsteroids.isEmpty() && acquiredWrecks.isEmpty()) return@launch//@runBlocking

		handleGeneration(
			GenerateChunk(
				generator,
				event.chunk.minecraft,
				acquiredWrecks,
				acquiredAsteroids
			),
			this
		)
	}

	private fun <T> search(
		generator: SpaceGenerator,
		chunkPos: ChunkPos,
		initialSeedOffset: Int = 0,
		callback: (chunkSeed: Long, chunkRandom: Random, maxHeight: Int, minHeight: Int, x: Int, y: Int, z: Int) -> T?
	): List<T> {
		val (centreChunkX, centreChunkZ) = chunkPos

		val radius = 10
		val radiusSquared = radius * radius

		val foundFeatures = mutableListOf<T>()

		val minHeight = generator.serverLevel.minBuildHeight
		val maxHeight = generator.serverLevel.maxBuildHeight
		val middleHeight = maxHeight / 2.0

		for (chunkXOffset in -radius..+radius) {
			val chunkXOffsetSquared = chunkXOffset * chunkXOffset
			val chunkX = chunkXOffset + centreChunkX
			val startX = chunkX.shl(4)
			val startXDouble = startX.toDouble()

			for (chunkZOffset in -radius..+radius) {
				val chunkZOffsetSquared = chunkZOffset * chunkZOffset
				val chunkZ = chunkZOffset + centreChunkZ
				val startZ = chunkZ.shl(4)
				val startZDouble = startZ.toDouble()

				var seedAdjust = initialSeedOffset

				if (chunkXOffsetSquared + chunkZOffsetSquared > radiusSquared) continue

				val chunkSeed = ChunkPos(chunkX, chunkZ).longKey

				val chunkDensity = generator.parseDensity(startXDouble, middleHeight, startZDouble)

				// random number out of 100, chance of asteroid's generation. For use in selection.
				for (count in 0..ceil(chunkDensity).toInt()) {

					// ensure placement of subsequent asteroids and wrecks aren't at the same coordinates
					val chunkRandom = Random(Random(chunkSeed).nextLong() + seedAdjust)
					seedAdjust += 33601

					val chance = chunkRandom.nextDouble(100.0)

					// Selects some asteroids that are generated. Allows for densities of 0<X<1 asteroids per chunk.
					if (chance > (chunkDensity * 10)) continue

					// Random coordinate generation.
					val x = chunkRandom.nextInt(0, 15) + startX
					val z = chunkRandom.nextInt(0, 15) + startZ
					val y = chunkRandom.nextInt(minHeight, maxHeight)

					foundFeatures.add(callback(chunkSeed, chunkRandom, maxHeight, minHeight, x, y, z) ?: continue)
				}
			}
		}

		return foundFeatures
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	suspend fun handleGeneration(task: GenerateChunk, scope: CoroutineScope) {
		task.generateChunk(scope)
		val completableData = task.returnData

		if (task.isCancelled) return

		task.returnData.invokeOnCompletion {
			val completed = completableData.getCompleted()

			Tasks.syncBlocking {
				completed.place(task.chunk.bukkitChunk)
				completed.store(task.chunk.bukkitChunk)
			}
		}
	}

	// Not the best implementation of this possible, it'll work for testing.
	suspend fun postGenerateFeature(task: GenerateChunk, scope: CoroutineScope) {
		val generator = task.generator

		lateinit var chunkXRange: IntRange
		lateinit var chunkZRange: IntRange

		val asteroid = task.asteroids.firstOrNull()

		asteroid?.let {
			val xRange = IntRange(
					asteroid.x - (asteroid.size * generator.searchRadius).toInt(),
					asteroid.x + (asteroid.size * generator.searchRadius).toInt()
				)
			val zRange = IntRange(
					asteroid.z - (asteroid.size * generator.searchRadius).toInt(),
					asteroid.z + (asteroid.size * generator.searchRadius).toInt()
				)

			chunkXRange = IntRange(xRange.first.shr(4), xRange.last.shr(4))
			chunkZRange = IntRange(zRange.first.shr(4), zRange.last.shr(4))
		}

		val wreck = task.wrecks.firstOrNull()

		wreck?.let {
			val clipboard: Clipboard = generator.schematicMap[wreck.wreckName]!!

			val region = clipboard.region.clone()
			val targetBlockVector: BlockVector3 = BlockVector3.at(wreck.x, wreck.y, wreck.z)
			val offset: BlockVector3 = targetBlockVector.subtract(clipboard.origin)

			region.shift(offset)

			val chunks = region.chunks

			chunkXRange = IntRange(chunks.minOf { it.x }, chunks.minOf { it.x })
			chunkZRange = IntRange(chunks.minOf { it.z }, chunks.minOf { it.z })
		}

		for (x in chunkXRange) {
			for (z in chunkZRange) {

				handleGeneration(
					GenerateChunk(
						generator,
						generator.serverLevel.world.getChunkAt(x, z).minecraft,
						listOfNotNull(wreck),
						listOfNotNull(asteroid)
					),
					scope
				)
			}
		}
	}
}
