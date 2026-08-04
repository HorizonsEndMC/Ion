package net.horizonsend.ion.server.features.starship.destruction

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarshipMechanics
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.destruction.AdvancedSinkProvider.Companion.getChunkMap
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement.AIR
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement.updateHeightMaps
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.playDirectionalStarshipSound
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.isBlockLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.Material
import org.bukkit.util.Vector
import java.util.LinkedList
import kotlin.math.sqrt
import kotlin.random.Random

open class BlackHoleSinkProvider(starship: ActiveStarship, val singularityLocation: Vector) : SinkProvider(starship) {
	private var iteration = 0
	private val maxIteration = sqrt(starship.blocks.size.toDouble())

	// Positions that have been obstructed. Blocks inside these will not be moved.
	private val obstructedPositions = LongOpenHashSet()

	// Prioritize the lowest positions first, so that the bottom iterates, then hits the ground, then everything above it, and so on.
	private var sinkPositions = longArrayOf()

	override fun setup() {
		// Populate the initial sinking list
		val newArray = starship.blocks.toLongArray()
		for (index in newArray.indices) {
			@Suppress("DEPRECATION")
			newArray[index] = toBlockKey(Vec3i(newArray[index]))
		}

		sinkPositions = newArray

		playSinkSound()
	}

	fun playSinkSound() {
		toPlayersInRadius(starship.centerOfMass.toLocation(starship.world), 1000.0 * 20.0) { player ->
			playDirectionalStarshipSound(
				starship.centerOfMass.toLocation(starship.world),
				player,
				starship.balancing.shipSounds.explodeNear,
				starship.balancing.shipSounds.explodeFar,
				1000.0
			)
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

		val newPositions = calculateNewPositions() ?: run {
			cancel()
			return
		}

		val oldChunkMap = getChunkMap(sinkPositions, starship.world)
		val newChunkMap = getChunkMap(newPositions, starship.world)

		val n = sinkPositions.size
		val capturedStates = Array(n) { AIR }
		val capturedTiles = Int2ObjectOpenHashMap<Pair<BlockState, CompoundTag>>()

		Tasks.syncBlocking {
			try {
				// Check for obstructions in the new positions

				// Remove the old blocks
				processOldBlocks(oldChunkMap, capturedStates, capturedTiles)

				// Replace 2% of tiles with lava
				removeCrossedPositions(capturedStates)
				randomizeLava(capturedStates)

				// A trimmed list of positions that were actually moved. Used to build the next set of blocks that will move.
				val trimmedPositions = processNewBlocks(newChunkMap, capturedStates)

				// Place tile entities in their new positions
				processTileEntities(capturedTiles, newPositions)

				// Broadcast changes
				OptimizedMovement.sendChunkUpdatesToPlayers(
					currentWorld = starship.world,
					newWorld = starship.world,
					chunkCache = Object2ObjectOpenHashMap(),
					oldChunkMap = oldChunkMap,
					newChunkMap = newChunkMap
				)

				// Save the moved blocks for their next iteration
				sinkPositions = trimmedPositions.toLongArray()

				intermittentExplosions()
			} catch (e: Throwable) {
				e.printStackTrace()
			}
		}
	}

	private var crossedSingularityPositions = mutableListOf<Int>()

	/**
	 * Moves the blocks, returns the list of new positions, the min, and max points.
	 **/
	private fun calculateNewPositions(): LongArray? {
		// Calculate the new positions from the velocity, and note the new min and max coordinates
		val newPositions = LongArray(sinkPositions.size) { index ->
			val it = sinkPositions[index]
			val currentVec3i = toVec3i(it)

			val oldRelativeX = currentVec3i.x - singularityLocation.x.toInt()
			val oldRelativeY = currentVec3i.y - singularityLocation.y.toInt()
			val oldRelativeZ = currentVec3i.z - singularityLocation.z.toInt()

//			val velocity = velocity
			val vector = singularityLocation.clone().subtract(currentVec3i.toVector())

			val velocity = (9.81) / (vector.length() / 100.0)

			val motion = Vec3i(vector.normalize().multiply(velocity))
			val newPos = toVec3i(it).plus(motion)

			val newRelativeX = newPos.x - singularityLocation.x.toInt()
			val newRelativeY = newPos.y - singularityLocation.y.toInt()
			val newRelativeZ = newPos.z - singularityLocation.z.toInt()

			// Check if it crosses the singularity
			if ((newRelativeX > 1 && oldRelativeX < 1) || (newRelativeX < 1 && oldRelativeX > 1)) crossedSingularityPositions.add(index)
			if ((newRelativeY > 1 && oldRelativeY < 1) || (newRelativeY < 1 && oldRelativeY > 1)) crossedSingularityPositions.add(index)
			if ((newRelativeZ > 1 && oldRelativeZ < 1) || (newRelativeZ < 1 && oldRelativeZ > 1)) crossedSingularityPositions.add(index)

			val newKey = toBlockKey(toVec3i(it).plus(motion))

			val x = newPos.x
			val y = newPos.y
			val z = newPos.z

			if (!starship.world.minecraft.isInWorldBounds(BlockPos(x, y, z))) return null

			newKey
		}

		return newPositions
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

					if (type.block is EntityBlock) {
						processOldTile(blockPos, nmsChunk, capturedTiles, index)
					}

					nmsChunk.`moonrise$getChunkHolder`().vanillaChunkHolder.blockChanged(blockPos)
					//TODO: PICK OUT FLAGS
					nmsChunk.level.sendBlockUpdated(blockPos, type, AIR, 0)

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
					nmsChunk.`moonrise$getChunkHolder`().vanillaChunkHolder.blockChanged(blockPos)
					//TODO: PICK OUT FLAGS
					nmsChunk.level.sendBlockUpdated(blockPos, AIR, data, 0)

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

	fun randomizeLava(capturedStates: Array<BlockState>) {
		val num = capturedStates.size / 50

		repeat(num) {
			val index = Random.nextInt(capturedStates.size)
			if (capturedStates[index].isAir) return@repeat
			capturedStates[index] = MAGMA_BLOCK_STATE
		}
	}

	fun removeCrossedPositions(capturedStates: Array<BlockState>) {
		for (index in crossedSingularityPositions) {
			capturedStates[index] = AIR
		}

		crossedSingularityPositions = mutableListOf()
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

	companion object {
		private val MAGMA_BLOCK_STATE = Material.MAGMA_BLOCK.createBlockData().nms
	}
}

