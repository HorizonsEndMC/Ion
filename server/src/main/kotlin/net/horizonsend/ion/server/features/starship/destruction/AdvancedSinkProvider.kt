package net.horizonsend.ion.server.features.starship.destruction

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement.AIR
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement.updateHeightMaps
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
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import java.util.PriorityQueue
import kotlin.math.sqrt
import kotlin.random.Random

class AdvancedSinkProvider(starship: ActiveStarship) : SinkProvider(starship) {
	private var velocity: Vec3i = Vec3i(0, -1, 0)

	private var iteration = 0
	private val maxIteration = sqrt(starship.blocks.size.toDouble())

	private val obstructedPositions = LongOpenHashSet()

	private val keyComparator = if (velocity.y != 0) Comparator { a: BlockKey, b: BlockKey -> getY(b).compareTo(getY(a)) }
		else if (velocity.x != 0) Comparator { a: BlockKey, b: BlockKey -> getX(b).compareTo(getX(a)) }
		else Comparator { a: BlockKey, b: BlockKey -> getZ(b).compareTo(getZ(a)) }

	// Prioritize the lowest positions first, so that the bottom iterates, then hits the ground, then everything above it, and so on.
	private var sinkPositions = PriorityQueue(keyComparator)

	private var minX = 0
	private var maxX = 0

	private var minY = 0
	private var maxY = 0

	private var minZ = 0
	private var maxZ = 0

	override fun setup() {
		if (starship.world.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
			velocity = Vec3i(
				Random.nextInt(-1, 1),
				Random.nextInt(-1, 1),
				Random.nextInt(-1, 1)
			)
		}

		// Populate the initial block queue
		sinkPositions.addAll(starship.blocks.map { toBlockKey(Vec3i(it)) })
	}

	override fun tick() {
		iteration++

		if (iteration > maxIteration || ActiveStarships.isActive(starship)) {
			cancel()
			Tasks.sync {
				finalExplosion()
			}

			return
		}

		val baseline = toVec3i(sinkPositions.first())

		var newMinX = baseline.x
		var newMaxX = baseline.x
		var newMinY = baseline.y
		var newMaxY = baseline.y
		var newMinZ = baseline.z
		var newMaxZ = baseline.z

		// Calculate the new positions from the velocity, and note the new min and max coordinates
		val newPositions = PriorityQueue<BlockKey> { a, b -> getY(b).compareTo(getY(a)) }
		sinkPositions.mapNotNullTo(newPositions) {
			if (obstructedPositions.contains(it)) return@mapNotNullTo null
			val newKey = toBlockKey(toVec3i(it).plus(velocity))

			val x = getX(newKey)
			val y = getY(newKey)
			val z = getZ(newKey)

			if (x > newMaxX) newMaxX = x
			if (x < newMinX) newMinX = x
			if (y > newMaxY) newMaxY = y
			if (y < newMinY) newMinY = y
			if (z > newMaxZ) newMaxZ = z
			if (z < newMinZ) newMinZ = z

			newKey
		}

		val oldChunkMap = getChunkMap(sinkPositions.toLongArray())
		val newChunkMap = getChunkMap(newPositions.toLongArray())

		val n = sinkPositions.size
		val capturedStates = Array(n) { AIR }
		val capturedTiles = mutableMapOf<Int, Pair<BlockState, CompoundTag>>()

		Tasks.syncBlocking {
			populateObstructionList(newPositions, newMinX, newMaxX, newMinY, newMaxY, newMinZ, newMaxZ)
			val lightModule = starship.world.minecraft.lightEngine

			for ((chunkKey, sectionMap) in oldChunkMap) {
				val chunk = starship.world.getChunkAt(chunkKeyX(chunkKey), chunkKeyZ(chunkKey))
				val nmsChunk = chunk.minecraft

				for ((sectionKey, positionMap) in sectionMap) {
					val section = nmsChunk.getSection(sectionKey)

					for ((blockKey, index) in positionMap) {
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

						lightModule.checkBlock(BlockPos(x, y, z)) // Lighting is not cringe
					}
				}
			}

			for ((chunkKey, sectionMap) in newChunkMap) {
				val chunk = starship.world.getChunkAt(chunkKeyX(chunkKey), chunkKeyZ(chunkKey))
				val nmsChunk = chunk.minecraft

				for ((sectionKey, positionMap) in sectionMap) {
					val section = nmsChunk.getSection(sectionKey)

					for ((blockKey, index) in positionMap) {
						val x = getX(blockKey)
						val y = getY(blockKey)
						val z = getZ(blockKey)

						val localX = x and 0xF
						val localY = y and 0xF
						val localZ = z and 0xF

						val data = capturedStates[index]

						val blockPos = BlockPos(x, y, z)
						nmsChunk.`moonrise$getChunkAndHolder`().holder.blockChanged(blockPos)
						nmsChunk.level.onBlockStateChange(blockPos, AIR /*TODO hangars */, data)

						section.setBlockState(localX, localY, localZ, data, false)
						lightModule.checkBlock(BlockPos(x, y, z))
					}
				}

				updateHeightMaps(nmsChunk)
				nmsChunk.markUnsaved()
			}

			OptimizedMovement.sendChunkUpdatesToPlayers(starship.world, starship.world, oldChunkMap, newChunkMap)
			maxX = newMaxX
			minX = newMinX
			maxY = newMaxY
			minY = newMinY
			maxZ = newMaxZ
			minZ = newMinZ

			sinkPositions = newPositions
		}
	}

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

//	/**
//	 * Moves the blocks, populates the obstruction queue, and returns the list of new positions
//	 **/
//	fun runMovementLoop(): Array<BlockKey> {
//
//	}

	private fun populateObstructionList(
		newPositions: PriorityQueue<BlockKey>,
		minX: Int = 0, maxX: Int = 0,
		minY: Int = 0, maxY: Int = 0,
		minZ: Int = 0, maxZ: Int = 0,
	) {
		for (position in newPositions) {
			if (obstructedPositions.contains(position)) continue
			val type = getBlockTypeSafe(starship.world, getX(position), getY(position), getZ(position))
			if (type == null || !type.isAir) addObstructedPosition(position, minX, maxX, minY, maxY, minZ, maxZ)
		}
	}

	private fun addObstructedPosition(
		position: BlockKey,
		minX: Int = 0, maxX: Int = 0,
		minY: Int = 0, maxY: Int = 0,
		minZ: Int = 0, maxZ: Int = 0,
	) {
		var nextPosition = toVec3i(position)
		obstructedPositions.add(position)

		var iterations = 0
		while (iterations < 100) {
			iterations++
			val newPosition = nextPosition.plus(velocity)
			if (obstructedPositions.contains(toBlockKey(newPosition))) break

			if (newPosition.x > maxX || newPosition.x < minX) break
			if (newPosition.y > maxY || newPosition.y < minY) break
			if (newPosition.z > maxZ || newPosition.z < minZ) break

			nextPosition = newPosition
		}
	}

	private fun finalExplosion() {

	}

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
