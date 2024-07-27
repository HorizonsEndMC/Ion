package net.horizonsend.ion.server.features.starship.destruction

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.starship.movement.ChunkMap
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement.getCollisionChunkMap
import net.horizonsend.ion.server.features.starship.movement.OptimizedMovement.updateHeightMaps
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.chunkKeyX
import net.horizonsend.ion.server.miscellaneous.utils.chunkKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import java.util.concurrent.ExecutionException

object SinkBlockMovement {
	fun moveBlocks(
		oldPositionArray: LongArray,
		newPositionArray: LongArray,
		sinkInformation: SinkInformation
	) {
		val oldChunkMap = OptimizedMovement.getChunkMap(oldPositionArray)
		val newChunkMap = OptimizedMovement.getChunkMap(newPositionArray)

		val collisionChunkMap = getCollisionChunkMap(oldChunkMap, newChunkMap)

		val n = oldPositionArray.size
		val capturedStates = java.lang.reflect.Array.newInstance(BlockState::class.java, n) as Array<BlockState>
		val capturedTiles = mutableMapOf<Int, Pair<BlockState, CompoundTag>>()

		val obstructedPositions = LongOpenHashSet()

		try {
			Tasks.syncBlocking {
				processCollisions(collisionChunkMap, newPositionArray, obstructedPositions, sinkInformation)

				processOldBlocks(
					sinkInformation,
					oldChunkMap,
					capturedStates,
					newPositionArray,
					obstructedPositions,
					capturedTiles
				)

				processNewBlocks(
					sinkInformation,
					newPositionArray,
					obstructedPositions,
					newChunkMap,
					capturedStates,
					capturedTiles
				)
			}
		} catch (e: ExecutionException) {
			throw e.cause ?: e
		}
	}

	fun buildNewPositionArray(oldPositionArray: LongArray, sinkInformation: SinkInformation): LongArray {
		val newLocationArray = LongArray(oldPositionArray.size)

		for (i in oldPositionArray.indices) {
			val blockKey = oldPositionArray[i]
			val x0 = blockKeyX(blockKey)
			val y0 = blockKeyY(blockKey)
			val z0 = blockKeyZ(blockKey)

			val x = x0 + sinkInformation.velocity.x
			val y = y0 + sinkInformation.velocity.y
			val z = z0 + sinkInformation.velocity.z

			val newBlockKey = blockKey(x, y, z)
			newLocationArray[i] = newBlockKey
		}

		return newLocationArray
	}

	private fun processCollisions(
		collisionChunkMap: ChunkMap,
		newPositionArray: LongArray,
		obstructedPositionArray: LongOpenHashSet,
		sinkInformation: SinkInformation
	) {
		for ((chunkKey, sectionMap) in collisionChunkMap) {
			val chunk = sinkInformation.world.getChunkAt(chunkKeyX(chunkKey), chunkKeyZ(chunkKey))
			val nmsChunk = chunk.minecraft

			for ((sectionKey, positionMap) in sectionMap) {
				val section = nmsChunk.sections[sectionKey]

				for ((blockKey, index) in positionMap) {
					check(newPositionArray[index] == blockKey)

					val x = blockKeyX(blockKey)
					val y = blockKeyY(blockKey)
					val z = blockKeyZ(blockKey)

					val localX = x and 0xF
					val localY = y and 0xF
					val localZ = z and 0xF

					val blockData = section.getBlockState(localX, localY, localZ)

					if (OptimizedMovement.passThroughBlocks.contains(blockData)) continue

					obstructedPositionArray.add(blockKey)
					newPositionArray[index] = -1
				}
			}
		}
	}

	private fun processOldBlocks(
		sinkInformation: SinkInformation,
		oldChunkMap: ChunkMap,
		capturedStates: Array<BlockState>,
		newPositionArray: LongArray,
		obstructedPositionArray: LongOpenHashSet,
		capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>
	) {
		val lightModule = sinkInformation.world.minecraft.lightEngine

		for ((chunkKey, sectionMap) in oldChunkMap) {
			val chunk = sinkInformation.world.getChunkAt(chunkKeyX(chunkKey), chunkKeyZ(chunkKey))
			val nmsChunk = chunk.minecraft

			for ((sectionKey, positionMap) in sectionMap) {
				val section = nmsChunk.getSection(sectionKey)

				for ((blockKey, index) in positionMap) {
					if (obstructedPositionArray.contains(newPositionArray[index])) {
						continue
					}

					val x = blockKeyX(blockKey)
					val y = blockKeyY(blockKey)
					val z = blockKeyZ(blockKey)

					val localX = x and 0xF
					val localY = y and 0xF
					val localZ = z and 0xF

					val type = section.getBlockState(localX, localY, localZ)
					capturedStates[index] = type

					if (type.block is BaseEntityBlock) {
						processOldTile(blockKey, nmsChunk, capturedTiles, index)
					}

					val blockPos = BlockPos(x, y, z)
					nmsChunk.playerChunk?.blockChanged(blockPos)
					nmsChunk.level.onBlockStateChange(blockPos, type, OptimizedMovement.AIR)

					section.setBlockState(localX, localY, localZ, OptimizedMovement.AIR, false)

					lightModule.checkBlock(BlockPos(x, y, z)) // Lighting is not cringe
				}
			}

			updateHeightMaps(nmsChunk)
			nmsChunk.isUnsaved = true
		}
	}

	private fun processNewBlocks(
		sinkInformation: SinkInformation,
		newPositionArray: LongArray,
		obstructedPositionArray: LongOpenHashSet,
		newChunkMap: ChunkMap,
		capturedStates: Array<BlockState>,
		capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>
	) {
		val lightModule = sinkInformation.world.minecraft.lightEngine

		for ((chunkKey, sectionMap) in newChunkMap) {
			val chunk = sinkInformation.world.getChunkAt(chunkKeyX(chunkKey), chunkKeyZ(chunkKey))
			val nmsChunk = chunk.minecraft

			for ((sectionKey, positionMap) in sectionMap) {
				val section = nmsChunk.getSection(sectionKey)

				for ((blockKey, index) in positionMap) {
					if (obstructedPositionArray.contains(newPositionArray[index])) {
						continue
					}

					val x = blockKeyX(blockKey)
					val y = blockKeyY(blockKey)
					val z = blockKeyZ(blockKey)

					val localX = x and 0xF
					val localY = y and 0xF
					val localZ = z and 0xF

					val data = capturedStates[index]

					val blockPos = BlockPos(x, y, z)
					nmsChunk.playerChunk?.blockChanged(blockPos)
					nmsChunk.level.onBlockStateChange(blockPos, OptimizedMovement.AIR, data)

					section.setBlockState(localX, localY, localZ, data, false)
					lightModule.checkBlock(BlockPos(x, y, z))
				}
			}

			updateHeightMaps(nmsChunk)
			nmsChunk.isUnsaved = true
		}

		for ((index, tile) in capturedTiles) {
			val blockKey = newPositionArray[index]
			val x = blockKeyX(blockKey)
			val y = blockKeyY(blockKey)
			val z = blockKeyZ(blockKey)

			val newPos = BlockPos(x, y, z)
			val chunk = sinkInformation.world.getChunkAt(x shr 4, z shr 4)

			val data = tile.first

			val blockEntity = BlockEntity.loadStatic(newPos, data, tile.second) ?: continue
			chunk.minecraft.addAndRegisterBlockEntity(blockEntity)
		}
	}

	private fun processOldTile(
		blockKey: Long,
		chunk: LevelChunk,
		capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>,
		index: Int
	) {
		val blockPos = BlockPos(
			blockKeyX(blockKey),
			blockKeyY(blockKey),
			blockKeyZ(blockKey)
		)

		val blockEntity = chunk.getBlockEntity(blockPos) ?: return
		capturedTiles[index] = Pair(blockEntity.blockState, blockEntity.saveWithFullMetadata())

		chunk.removeBlockEntity(blockPos)
	}
}
