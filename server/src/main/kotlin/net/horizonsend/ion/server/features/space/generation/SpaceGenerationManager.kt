package net.horizonsend.ion.server.features.space.generation

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.encounters.Encounters
import net.horizonsend.ion.server.features.space.generation.generators.AsteroidGenerationData
import net.horizonsend.ion.server.features.space.generation.generators.GenerateAsteroidTask
import net.horizonsend.ion.server.features.space.generation.generators.GenerateWreckTask
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerationTask
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerator
import net.horizonsend.ion.server.features.space.generation.generators.WreckGenerationData
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
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
import org.bukkit.persistence.PersistentDataType
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

	// Generate asteroids on chunk load
	@EventHandler
	fun onChunkLoad(event: ChunkLoadEvent) = coroutineScope.launch {
		val generator = getGenerator((event.world as CraftWorld).handle) ?: return@launch
		if (event.chunk.persistentDataContainer.has(NamespacedKeys.SPACE_GEN_VERSION)) return@launch

		event.chunk.persistentDataContainer.set(
			NamespacedKeys.SPACE_GEN_VERSION,
			PersistentDataType.BYTE,
			generator.spaceGenerationVersion
		)

		val chunkPos = event.chunk.minecraft.pos

		// Generate a number of random asteroids in a chunk, proportional to the density in a portion of the chunk. Allows densities of X>1 asteroid per chunk.
		launch {
			// Asteroids
			val acquiredAsteroids = searchAsteroids(generator, chunkPos)

			if (acquiredAsteroids.isNotEmpty()) {
				generateFeature(
					GenerateAsteroidTask(
						generator,
						chunkPos,
						acquiredAsteroids
					),
					this
				)
			}

			// Wrecks
			val acquiredWrecks = searchWrecks(generator, chunkPos)

			if (acquiredWrecks.isNotEmpty()) {
				generateFeature(
					GenerateWreckTask(
						generator,
						chunkPos,
						acquiredWrecks
					),
					this
				)
			}
		}
	}

	private fun searchAsteroids(generator: SpaceGenerator, chunkPos: ChunkPos): List<AsteroidGenerationData> {
		val (x, z) = chunkPos
		val cornerX = x.shl(4)
		val cornerZ = z.shl(4)

		val radius = 10

		val radiusSquared = radius * radius

		val nearbyFeatures = mutableListOf<AsteroidGenerationData>()

		val minHeight = generator.serverLevel.minBuildHeight
		val maxHeight = generator.serverLevel.maxBuildHeight
		val middleHeight = maxHeight / 2.0

		for (iteratedX in -radius..+radius) {
			val iteratedXSquared = iteratedX * iteratedX
			val realX = iteratedX + x
			val worldX = realX.shl(4)
			val worldXDouble = worldX.toDouble()

			for (iteratedZ in -radius..+radius) {
				val iteratedZSquared = iteratedZ * iteratedZ
				val realZ = iteratedZ + z
				val worldZ = realZ.shl(4)

				if (iteratedXSquared + iteratedZSquared > radiusSquared) continue

				val chunkSeed = ChunkPos(realX, realZ).longKey

				val chunkRandom = Random(Random(chunkSeed).nextLong())

				val chunkDensity = generator.parseDensity(
					worldXDouble,
					middleHeight,
					worldZ.toDouble()
				)

				for (count in 0..ceil(chunkDensity).toInt()) {
					// random number out of 100, chance of asteroid's generation. For use in selection.
					val chance = chunkRandom.nextDouble(100.0)

					// Selects some asteroids that are generated. Allows for densities of 0<X<1 asteroids per chunk.
					if (chance > (chunkDensity * 10)) continue

					// Random coordinate generation.
					val asteroidX = chunkRandom.nextInt(0, 15) + worldX
					val asteroidZ = chunkRandom.nextInt(0, 15) + worldZ
					val asteroidY = chunkRandom.nextInt(minHeight, maxHeight)

					val asteroid = generator.generateWorldAsteroid(
						chunkSeed,
						chunkRandom,
						maxHeight,
						minHeight,
						asteroidX,
						asteroidY,
						asteroidZ
					)

					val distance = distance(cornerX, cornerZ, asteroid.x, asteroid.z)

					if (distance > (asteroid.size * 1.25)) continue

					nearbyFeatures.add(asteroid)
				}
			}
		}

		return nearbyFeatures
	}

	private fun searchWrecks(generator: SpaceGenerator, chunkPos: ChunkPos): List<WreckGenerationData> {
		val (x, z) = chunkPos

		val radius = 9

		val radiusSquared = radius * radius
		val nearbyFeatures = mutableListOf<WreckGenerationData>()

		val minHeight = generator.serverLevel.minBuildHeight
		val maxHeight = generator.serverLevel.maxBuildHeight
		val middleHeight = maxHeight / 2.0

		for (iteratedX in -radius..+radius) {
			val iteratedXSquared = iteratedX * iteratedX
			val realX = iteratedX + x
			val worldX = realX.shl(4)
			val worldXDouble = worldX.toDouble()

			for (iteratedZ in -radius..+radius) {
				val iteratedZSquared = iteratedZ * iteratedZ
				val realZ = iteratedZ + z
				val worldZ = realZ.shl(4)

				if (iteratedXSquared + iteratedZSquared > radiusSquared) continue

				val chunkSeed = ChunkPos(realX, realZ).longKey

				val chunkRandom = Random(Random(chunkSeed).nextLong())

				val chunkDensity = generator.parseDensity(
					worldXDouble,
					middleHeight,
					worldZ.toDouble()
				)

				for (count in 0..ceil(chunkDensity).toInt()) {
					// random number out of 100, chance of asteroid's generation. For use in selection.
					val chance = chunkRandom.nextDouble(100.0)

					// Selects some asteroids that are generated. Allows for densities of 0<X<1 asteroids per chunk.
					if (chance > (chunkDensity * 10)) continue

					// Random coordinate generation.
					val wreckX = chunkRandom.nextInt(0, 15) + worldX
					val wreckZ = chunkRandom.nextInt(0, 15) + worldZ
					val wreckY = chunkRandom.nextInt(minHeight, maxHeight)

					val wreck = generator.generateRandomWreckData(
						wreckX,
						wreckY,
						wreckZ
					)

					nearbyFeatures.add(wreck)
				}
			}
		}

		return nearbyFeatures
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	suspend fun generateFeature(task: SpaceGenerationTask<*>, scope: CoroutineScope) {
		task.generateChunk(scope)
		val completableData = task.returnData

		task.returnData.invokeOnCompletion {
			val completed = completableData.getCompleted()

			task.postProcessASync(completed)

			Tasks.syncBlocking {
				val chunk = completed.finishPlacement(task.chunk, task.generator)

				val serializedWreck = (completed as? WreckGenerationData.WreckReturnData)?.serializedWreckData

				task.postProcessSync(completed)

				serializedWreck?.let { wreck ->
					val encounter = Encounters[wreck] ?: return@let

					encounter.generate(
						task.generator.serverLevel.world,
						wreck.getInt("x"),
						wreck.getInt("y"),
						wreck.getInt("z")
					)
				}

				completed.store(task.generator, chunk)
			}
		}
	}

	// Not the best implementation of this possible, it'll work for testing. Also I wanted to test stuff with generics.
	suspend fun <T: SpaceGenerationTask<*>> postGenerateFeature(task: T, scope: CoroutineScope) {
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
					val chunkPos = ChunkPos(x, z)

					generateFeature(
						GenerateAsteroidTask(
							generator,
							chunkPos,
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

			for (chunk in chunks) {
				val bukkitChunk = ChunkPos(chunk.x, chunk.z)

				generateFeature(
					GenerateWreckTask(
					generator,
					bukkitChunk,
					listOf(wreck)
					),
					scope
				)
			}
		}
	}
}
