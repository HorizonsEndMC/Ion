package net.horizonsend.ion.server.features.space.generation

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.place
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks.Companion.store
import net.horizonsend.ion.server.features.space.generation.generators.GenerateAsteroidTask
import net.horizonsend.ion.server.features.space.generation.generators.GenerateWreckTask
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerationTask
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
	fun onChunkLoadEvent(event: ChunkLoadEvent) = runBlocking {
		val generator = getGenerator(event.world.minecraft) ?: return@runBlocking
		if (event.chunk.persistentDataContainer.has(SPACE_GEN_VERSION)) return@runBlocking

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

		if (acquiredAsteroids.isNotEmpty()) {
			generateFeature(GenerateAsteroidTask(generator, event.chunk.minecraft, acquiredAsteroids), this)
		}

		// Wrecks
		val acquiredWrecks = search(generator, chunkPos) { _, chunkRandom, _, _, x, y, z ->
			generator.generateRandomWreckData(chunkRandom, x, y, z)
		}

		if (acquiredWrecks.isNotEmpty()) {
			generateFeature(GenerateWreckTask(generator, event.chunk.minecraft, acquiredWrecks), this)
		}
	}

	private fun <T> search(
		generator: SpaceGenerator,
		chunkPos: ChunkPos,
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

				if (chunkXOffsetSquared + chunkZOffsetSquared > radiusSquared) continue

				val chunkSeed = ChunkPos(chunkX, chunkZ).longKey
				val chunkRandom = Random(Random(chunkSeed).nextLong())

				val chunkDensity = generator.parseDensity(startXDouble, middleHeight, startZDouble)

				for (count in 0..ceil(chunkDensity).toInt()) {
					// random number out of 100, chance of asteroid's generation. For use in selection.
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
	suspend fun generateFeature(task: SpaceGenerationTask, scope: CoroutineScope) {
		task.generateChunk(scope)
		val completableData = task.returnData

		task.returnData.invokeOnCompletion {
			val completed = completableData.getCompleted()

			task.postProcessASync(completed)

			Tasks.syncBlocking {
				completed.place(task.chunk.bukkitChunk)

				task.postProcessSync(completed)

				completed.store(task.chunk.bukkitChunk)
			}
		}
	}

	// Not the best implementation of this possible, it'll work for testing. Also I wanted to test stuff with generics.
	suspend fun <T: SpaceGenerationTask> postGenerateFeature(task: T, scope: CoroutineScope) {
		val generator = task.generator

		(task as? GenerateAsteroidTask)?.let {
			val asteroid = task.asteroids.first()

			val xRange =
				IntRange(
					asteroid.x - (asteroid.size * generator.searchRadius).toInt(),
					asteroid.x + (asteroid.size * generator.searchRadius).toInt()
				)
			val zRange =
				IntRange(
					asteroid.z - (asteroid.size * generator.searchRadius).toInt(),
					asteroid.z + (asteroid.size * generator.searchRadius).toInt()
				)

			val chunkXRange = IntRange(xRange.first.shr(4), xRange.last.shr(4))
			val chunkZRange = IntRange(zRange.first.shr(4), zRange.last.shr(4))

			for (x in chunkXRange) {
				for (z in chunkZRange) {

					generateFeature(
						GenerateAsteroidTask(
							generator,
							generator.serverLevel.world.getChunkAt(x, z).minecraft,
							listOf(asteroid)
						),
						scope
					)
				}
			}
		}

		(task as? GenerateWreckTask)?.let {
			val wreck = task.chunkCoveredWrecks.first()
			val clipboard: Clipboard = generator.schematicMap[wreck.wreckName]!!

			val region = clipboard.region.clone()
			val targetBlockVector: BlockVector3 = BlockVector3.at(wreck.x, wreck.y, wreck.z)
			val offset: BlockVector3 = targetBlockVector.subtract(clipboard.origin)

			region.shift(offset)

			val chunks = region.chunks

			for (iterated in chunks) {
				generateFeature(
					GenerateWreckTask(
						generator,
						generator.serverLevel.world.getChunkAt(iterated.x, iterated.z).minecraft,
						listOf(wreck)
					),
					scope
				)
			}
		}
	}
}
