package net.horizonsend.ion.server.features.starship.destruction

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.nations.utils.playSoundInRadius
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipMechanics
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement.AIR
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement.updateHeightMaps
import net.horizonsend.ion.server.features.starship.subsystem.checklist.SupercapitalReactorSubsystem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isBlockLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.util.Vector
import java.util.LinkedList
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

open class AdvancedSinkProvider(starship: ActiveStarship) : SinkProvider(starship) {
	private var velocity: Vec3i = Vec3i(0, -1, 0)
		set(value) {
			field = value
			inverseVelocity = Vec3i(-value.x, -value.y, -value.z)
		}

	private var inverseVelocity = Vec3i(-velocity.x, -velocity.y, -velocity.z)

	private var iteration = 0
	private val maxIteration = sqrt(starship.blocks.size.toDouble())

	// Positions that have been obstructed. Blocks inside these will not be moved.
	private val obstructedPositions = LongOpenHashSet()

	// Prioritize the lowest positions first, so that the bottom iterates, then hits the ground, then everything above it, and so on.
	private var sinkPositions = longArrayOf()

	override fun setup() {
		// Handle random velocity in space
		if (starship.world.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
			velocity = Vec3i(
				Random.nextInt(-1, 1),
				Random.nextInt(-1, 1),
				Random.nextInt(-1, 1)
			)
			if (velocity.x == 0 && velocity.x == 0 && velocity.y == 0) velocity = Vec3i(1, 1, 1)
		}

		// Populate the initial sinking list
		val newArray = starship.blocks.toLongArray()
		for (index in newArray.indices) {
			@Suppress("DEPRECATION")
			newArray[index] = toBlockKey(Vec3i(newArray[index]))
		}

		sinkPositions = newArray

		tryReactorParticles()
		playSinkSound()
	}

	private fun tryReactorParticles() {
		val reactor = starship.subsystems.filterIsInstance<SupercapitalReactorSubsystem<*>>().firstOrNull() ?: return
		val center = (reactor.pos).toCenterVector()

		val tickRate = 4L
		val growRate = 4.5 * (tickRate.toDouble() / 20.0)

		var particleRadius = 0.0

		runnable {
			particleRadius += growRate
			val number = (2.0 * PI * particleRadius).toInt()

			for (count in 0..number) {
				// Get the fraction around the circle
				val degrees = 360.0 - (360.0 * (count.toDouble() / number.toDouble()))
				val radians = degrees * (PI / 180.0)

				val xOffset = cos(radians) * particleRadius
				val zOffset = sin(radians) * particleRadius

				val newLoc = center.clone().add(Vector(xOffset, 0.0, zOffset))

				starship.world.spawnParticle(Particle.SONIC_BOOM, newLoc.x, newLoc.y, newLoc.z, 1, 0.0, 0.0, 0.0, 0.0, null,true)

				if (particleRadius >= 150.0) cancel()
			}
		}.runTaskTimer(IonServer, tickRate, tickRate)
	}

	fun playSinkSound() {
		starship.balancing.sounds.explode?.let {
			playSoundInRadius(starship.centerOfMass.toLocation(starship.world), 7_500.0, it.sound)
		}
	}

	override fun cancel() {
		super.cancel()

		Tasks.sync {
			finalExplosion()
		}
	}

	override fun tick() {
		iteration++
		if (iteration > maxIteration || ActiveStarships.isActive(starship) || sinkPositions.isEmpty()) {
			cancel()
			return
		}

		val (newPositions, minPoint, maxPoint) = calculateNewPositions() ?: run {
			cancel()
			return
		}

		val oldChunkMap = getChunkMap(sinkPositions)
		val newChunkMap = getChunkMap(newPositions)

		val n = sinkPositions.size
		val capturedStates = Array(n) { AIR }
		val capturedTiles = mutableMapOf<Int, Pair<BlockState, CompoundTag>>()

		Tasks.syncBlocking {
			try {
				// Check for obstructions in the new positions
				populateObstructionList(newPositions, minPoint, maxPoint)

				// Remove the old blocks
				processOldBlocks(oldChunkMap, capturedStates, capturedTiles)

				// A trimmed list of positions that were actually moved. Used to build the next set of blocks that will move.
				val trimmedPositions = processNewBlocks(newChunkMap, capturedStates)

				// Place tile entities in their new positions
				processTileEntities(capturedTiles, newPositions)

				// Broadcast changes
				OptimizedMovement.sendChunkUpdatesToPlayers(starship.world, starship.world, oldChunkMap, newChunkMap)

				// Save the moved blocks for their next iteration
				sinkPositions = trimmedPositions.toLongArray()

				intermittentExplosions()
			} catch (e: Throwable) {
				e.printStackTrace()
			}
		}
	}

	/**
	 * Moves the blocks, returns the list of new positions, the min, and max points.
	 **/
	private fun calculateNewPositions(): Triple<LongArray, Vec3i, Vec3i>? {
		val baseline = toVec3i(sinkPositions.first())

		var newMinX = baseline.x
		var newMaxX = baseline.x
		var newMinY = baseline.y
		var newMaxY = baseline.y
		var newMinZ = baseline.z
		var newMaxZ = baseline.z

		// Calculate the new positions from the velocity, and note the new min and max coordinates
		val newPositions = LongArray(sinkPositions.size) { index ->
			val it = sinkPositions[index]

			val newKey = toBlockKey(toVec3i(it).plus(velocity))

			val x = getX(newKey)
			val y = getY(newKey)
			val z = getZ(newKey)

			if (!starship.world.minecraft.isInWorldBounds(BlockPos(x, y, z))) return null

			if (x > newMaxX) newMaxX = x
			if (x < newMinX) newMinX = x
			if (y > newMaxY) newMaxY = y
			if (y < newMinY) newMinY = y
			if (z > newMaxZ) newMaxZ = z
			if (z < newMinZ) newMinZ = z

			newKey
		}

		return Triple(newPositions, Vec3i(newMinX, newMinY, newMinZ), Vec3i(newMaxX, newMaxY, newMaxZ))
	}

	private fun populateObstructionList(newPositions: LongArray, minPoint: Vec3i, maxPoint: Vec3i) {
		for (position in newPositions) {
			if (obstructedPositions.contains(position)) continue
			if (sinkPositions.contains(position)) continue

			if (!starship.world.minecraft.isInWorldBounds(BlockPos.of(position))) {
				addObstructedPosition(position, minPoint, maxPoint)
				continue
			}

			val type = getBlockTypeSafe(starship.world, getX(position), getY(position), getZ(position))
			if (type == null || !type.isAir) addObstructedPosition(position, minPoint, maxPoint)
		}
	}

	/**
	 * Adds the provided position to the obstructed list, and searches out in the opposite direction of the velocity.
	 **/
	private fun addObstructedPosition(position: BlockKey, minPoint: Vec3i, maxPoint: Vec3i) {
		var nextPosition = toVec3i(position)
		obstructedPositions.add(position)

		// Force upper bound to avoid chance of an infinite loop
		var iterations = 0
		while (iterations < 100) {
			iterations++
			val newPosition = nextPosition.plus(inverseVelocity)
			if (obstructedPositions.contains(toBlockKey(newPosition))) break

			if (newPosition.x > maxPoint.x || newPosition.x < minPoint.x) break
			if (newPosition.y > maxPoint.y || newPosition.y < minPoint.y) break
			if (newPosition.z > maxPoint.z || newPosition.z < minPoint.z) break

			nextPosition = newPosition
			obstructedPositions.add(toBlockKey(newPosition))
		}
	}

	private fun processOldBlocks(oldChunkMap: SinkChunkMap, capturedStates: Array<BlockState>, capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>) {
		val lightModule = starship.world.minecraft.lightEngine

		for ((chunkKey, sectionMap) in oldChunkMap) {
			val chunk = starship.world.getChunkAt(chunkKeyX(chunkKey), chunkKeyZ(chunkKey))
			val nmsChunk = chunk.minecraft

			for ((sectionKey, positionMap) in sectionMap) {
				val section = nmsChunk.getSection(sectionKey)

				for ((blockKey, index) in positionMap) {
					if (obstructedPositions.contains(blockKey)) continue

					val x = getX(blockKey)
					val y = getY(blockKey)
					val z = getZ(blockKey)

					val localX = x and 0xF
					val localY = y and 0xF
					val localZ = z and 0xF

					val type = section.getBlockState(localX, localY, localZ)
					if (type.isAir) continue

					capturedStates[index] = type
					val blockPos = BlockPos(x, y, z)

					if (type.block is BaseEntityBlock) {
						processOldTile(blockPos, nmsChunk, capturedTiles, index)
					}

					nmsChunk.`moonrise$getChunkAndHolder`().holder.blockChanged(blockPos)
					nmsChunk.level.onBlockStateChange(blockPos, type, AIR)

					section.setBlockState(localX, localY, localZ, AIR, false)

					lightModule.checkBlock(BlockPos(x, y, z))
				}
			}
		}
	}

	/**
	 * Removes old block, and saves the block entity data if there is one present.
	 **/
	private fun processOldTile(
		blockPos: BlockPos,
		chunk: LevelChunk,
		capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>,
		index: Int,
	) {
		val blockEntity = chunk.getBlockEntity(blockPos) ?: return
		capturedTiles[index] = Pair(blockEntity.blockState, blockEntity.saveWithFullMetadata(chunk.level.registryAccess()))

		chunk.removeBlockEntity(blockPos)
	}

	private fun processNewBlocks(newChunkMap: SinkChunkMap, capturedStates: Array<BlockState>): LongOpenHashSet {
		val trimmedPositions = LongOpenHashSet()
		val lightModule = starship.world.minecraft.lightEngine

		for ((chunkKey, sectionMap) in newChunkMap) {
			val chunk = starship.world.getChunkAt(chunkKeyX(chunkKey), chunkKeyZ(chunkKey))
			val nmsChunk = chunk.minecraft

			for ((sectionKey, positionMap) in sectionMap) {
				val section = nmsChunk.getSection(sectionKey)

				for ((blockKey, index) in positionMap) {
					if (obstructedPositions.contains(blockKey)) continue

					val x = getX(blockKey)
					val y = getY(blockKey)
					val z = getZ(blockKey)

					val localX = x and 0xF
					val localY = y and 0xF
					val localZ = z and 0xF

					val data = capturedStates[index]
					if (data.isAir) continue

					val blockPos = BlockPos(x, y, z)
					nmsChunk.`moonrise$getChunkAndHolder`().holder.blockChanged(blockPos)
					nmsChunk.level.onBlockStateChange(blockPos, AIR, data)

					section.setBlockState(localX, localY, localZ, data, false)
					lightModule.checkBlock(BlockPos(x, y, z))
					trimmedPositions.add(blockKey)
				}
			}

			updateHeightMaps(nmsChunk)
			nmsChunk.markUnsaved()
		}

		return trimmedPositions
	}

	private fun processTileEntities(capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>, newPositions: LongArray) {
		for ((index, tile) in capturedTiles) {
			val blockKey = newPositions[index]
			if (obstructedPositions.contains(blockKey)) continue

			val x = getX(blockKey)
			val y = getY(blockKey)
			val z = getZ(blockKey)

			val newPos = BlockPos(x, y, z)
			val chunk = starship.world.getChunkAt(x shr 4, z shr 4)

			val blockEntity = BlockEntity.loadStatic(
				newPos,
				tile.first,
				tile.second,
				starship.world.minecraft.registryAccess()
			) ?: continue

			chunk.minecraft.addAndRegisterBlockEntity(blockEntity)
		}
	}

	private fun intermittentExplosions() {
		val random = sinkPositions.randomOrNull() ?: return
		val (x, y, z) = toVec3i(random)

		if (isBlockLoaded(starship.world, x, y, z)) {
			Tasks.sync { starship.world.createExplosion(x.toDouble(), y.toDouble(), z.toDouble(), 8.0f) }
		}
	}

	private fun finalExplosion() {
		var i = 0
		val blockInterval = 500
		val queueInterval = 200
		val ticksBetweenExplosions = 4L

		val queue = LinkedList<Long>()

		for (block in sinkPositions.iterator()) {
			i++

			if (i % queueInterval == 0) {
				queue.add(block)
			}

			if (i % blockInterval != 0) {
				continue
			}

			val x = getX(block).toDouble()
			val y = getY(block).toDouble()
			val z = getZ(block).toDouble()

			val delay = ticksBetweenExplosions * (i / blockInterval)
			Tasks.syncDelayTask(delay) {
				if (isBlockLoaded(starship.world, x, y, z)) {
					ActiveStarshipMechanics.withBlockExplosionDamageAllowed {
						starship.world.createExplosion(x, y, z, 6.0f)
					}
				}
			}
		}

		val finalDelay = ticksBetweenExplosions * (i / blockInterval) + 10

		Tasks.syncDelayTask(finalDelay) {
			for (block in queue) {
				val x = getX(block).toDouble()
				val y = getY(block).toDouble()
				val z = getZ(block).toDouble()

				if (isBlockLoaded(starship.world, x, y, z)) ActiveStarshipMechanics.withBlockExplosionDamageAllowed {
					starship.world.createExplosion(x, y, z, 8.0f)
				}
			}
		}

		if (starship.world.hasFlag(WorldFlag.ARENA) ) {
			val air = Material.AIR.createBlockData()

			Tasks.syncDelayTask(finalDelay) {
				for (key in sinkPositions.iterator()) {
					starship.world.getBlockAt(getX(key), getY(key), getZ(key)).setBlockData(air, false)
				}
			}
		}
	}

	/**
	 * Formnats the provided position array into the chunk map
	 **/
	private fun getChunkMap(positionArray: LongArray): SinkChunkMap {
		val chunkMap = mutableMapOf<Long, MutableMap<Int, MutableMap<Long, Int>>>()

		for (index in positionArray.indices) {
			val blockKey = positionArray[index]

			val x = getX(blockKey)
			val y = getY(blockKey)
			val z = getZ(blockKey)

			val chunkKey = chunkKey(x shr 4, z shr 4)

			val sectionKey = starship.world.minecraft.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(y))

			val sectionMap = chunkMap.getOrPut(chunkKey) { mutableMapOf() }
			val positionMap = sectionMap.getOrPut(sectionKey) { mutableMapOf() }

			positionMap[blockKey] = index
		}

		return chunkMap
	}
}

/**
 * Map of a chunk key to a map of a section key to a map of a block key to its index in the block list
 **/
private typealias SinkChunkMap = Map<Long, Map<Int, Map<BlockKey, Int>>>
