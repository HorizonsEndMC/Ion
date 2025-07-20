package net.horizonsend.ion.server.features.starship.movement

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet
import it.unimi.dsi.fastutil.shorts.ShortSet
import net.horizonsend.ion.server.features.starship.Hangars
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.chunkKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ChunkHolder
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.BambooSaplingBlock
import net.minecraft.world.level.block.BambooStalkBlock
import net.minecraft.world.level.block.BaseCoralPlantTypeBlock
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.DoublePlantBlock
import net.minecraft.world.level.block.FungusBlock
import net.minecraft.world.level.block.GlowLichenBlock
import net.minecraft.world.level.block.GrowingPlantBlock
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.NetherPortalBlock
import net.minecraft.world.level.block.StainedGlassBlock
import net.minecraft.world.level.block.VineBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.Heightmap
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import java.util.LinkedList
import java.util.concurrent.ExecutionException

object OptimizedMovement {
	private val passThroughBlocks = listOf(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.SNOW).mapTo(ObjectOpenHashSet()) { it.createBlockData().nms }

	fun moveStarship(
		executionCheck: () -> Boolean,
		world1: World,
		world2: World,
		oldPositionArray: LongArray,
		newPositionArray: LongArray,
		blockStateTransform: (BlockState) -> BlockState,
		callback: () -> Unit
	) {
		val oldChunkMap = getChunkMap(oldPositionArray)
		val newChunkMap = getChunkMap(newPositionArray)
		val collisionChunkMap = getCollisionChunkMap(oldChunkMap, newChunkMap)

		val n = oldPositionArray.size
		val capturedStates = arrayOfNulls<BlockState>(n) as Array<BlockState>
		val capturedTiles = Int2ObjectOpenHashMap<Pair<BlockState, CompoundTag>>()
		val hangars = LinkedList<Long>()

		try {
			Tasks.syncBlocking {
				if (!executionCheck.invoke()) {
					return@syncBlocking
				}

				val chunkCache = Long2ObjectOpenHashMap<LevelChunk>()

				checkForCollision(world2, collisionChunkMap, chunkCache, hangars, newPositionArray)

				processOldBlocks(
					oldChunkMap,
					world1,
					world2,
					chunkCache,
					capturedStates,
					capturedTiles
				)

				dissipateHangarBlocks(world2, hangars)

				processNewBlocks(
					newPositionArray,
					newChunkMap,
					world1,
					world2,
					chunkCache,
					capturedStates,
					capturedTiles,
					blockStateTransform
				)

				callback()

				sendChunkUpdatesToPlayers(world1, world2, chunkCache, oldChunkMap, newChunkMap)
			}
		} catch (e: ExecutionException) {
			throw e.cause ?: e
		}
	}

	private fun checkForCollision(
		world: World,
		collisionChunkMap: ChunkMap,
		chunkCache: Long2ObjectOpenHashMap<LevelChunk>,
		hangars: LinkedList<Long>,
		newPositionArray: LongArray
	) {
		for ((chunkKey, sectionMap) in collisionChunkMap) {
			val nmsChunk = getNMSChunk(chunkCache, world, chunkKey)

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

					if (!passThroughBlocks.contains(blockData)) {
						if (!isHangar(blockData)) {
							throw StarshipBlockedException(Vec3i(x, y, z), blockData)
						}

						hangars.add(blockKey)
					}
				}
			}
		}
	}

	private fun isHangar(newBlockData: BlockState) =
		newBlockData.block is StainedGlassBlock ||
			newBlockData.block is NetherPortalBlock ||
			newBlockData.block is LiquidBlock ||
			newBlockData.block is BushBlock || // most types of crop/grass blocks
			newBlockData.block is VineBlock || // normal vines
			newBlockData.block is GrowingPlantBlock || // twisted vines on Luxiterna, kelp, etc.
			newBlockData.block is LeavesBlock ||
			newBlockData.block is BaseCoralPlantTypeBlock ||
			newBlockData.block is BambooSaplingBlock ||
			newBlockData.block is BambooStalkBlock ||
			newBlockData.block is FungusBlock ||
			newBlockData.block is DoublePlantBlock ||
			newBlockData.block is GlowLichenBlock

	private fun dissipateHangarBlocks(world2: World, hangars: LinkedList<Long>) {
		for (blockKey in hangars.iterator()) {
			Hangars.dissipateBlock(world2, blockKey)
		}
	}

	val AIR: BlockState = Blocks.AIR.defaultBlockState()

	private fun processOldBlocks(
		oldChunkMap: ChunkMap,
		world1: World,
		world2: World,
		chunkCache: Long2ObjectOpenHashMap<LevelChunk>,
		capturedStates: Array<BlockState>,
		capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>
	) {
		val lightModule = world1.minecraft.lightEngine
		val relightChunks = ObjectOpenHashSet<ChunkPos>()

		for ((chunkKey, sectionMap) in oldChunkMap) {
			val nmsChunk = getNMSChunk(chunkCache, world1, chunkKey)
			relightChunks.add(nmsChunk.pos)

			for ((sectionKey, positionMap) in sectionMap) {
				val section = nmsChunk.getSection(sectionKey)

				for ((blockKey, index) in positionMap) {
					val x = blockKeyX(blockKey)
					val y = blockKeyY(blockKey)
					val z = blockKeyZ(blockKey)

					val localX = x and 0xF
					val localY = y and 0xF
					val localZ = z and 0xF

					val type = section.getBlockState(localX, localY, localZ)
					capturedStates[index] = type

					val blockPos = BlockPos(x, y, z)
					if (type.block is BaseEntityBlock) {
						processOldTile(blockPos, nmsChunk, capturedTiles, index)
					}

					pseudoBlockChanged(nmsChunk, sectionKey, blockPos)
					nmsChunk.level.onBlockStateChange(blockPos, type, AIR)

					section.setBlockState(localX, localY, localZ, AIR, false)
//					lightModule.`starlight$getLightEngine`().serverLightQueue.queueBlockChange(BlockPos(x, y, z))
				}

//				lightModule.updateSectionStatus(SectionPos.of(chunk.x, sectionKey, chunk.z), false)
			}

			updateHeightMaps(nmsChunk)
			nmsChunk.markUnsaved()
		}

		lightModule.`starlight$serverRelightChunks`(relightChunks, {}, {})
	}

	private fun processNewBlocks(
		newPositionArray: LongArray,
		newChunkMap: ChunkMap,
		world1: World,
		world2: World,
		chunkCache: Long2ObjectOpenHashMap<LevelChunk>,
		capturedStates: Array<BlockState>,
		capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>,
		blockDataTransform: (BlockState) -> BlockState
	) {
		val lightModule = world2.minecraft.lightEngine
		val relightChunks = ObjectOpenHashSet<ChunkPos>()

		for ((chunkKey, sectionMap) in newChunkMap) {
			val nmsChunk = getNMSChunk(chunkCache, world2, chunkKey)
			relightChunks.add(nmsChunk.pos)

			for ((sectionKey, positionMap) in sectionMap) {
				val section = nmsChunk.getSection(sectionKey)

				for ((blockKey, index) in positionMap) {
					val x = blockKeyX(blockKey)
					val y = blockKeyY(blockKey)
					val z = blockKeyZ(blockKey)

					val localX = x and 0xF
					val localY = y and 0xF
					val localZ = z and 0xF

					// TODO: Save hangars
					val data = blockDataTransform(capturedStates[index])

					val blockPos = BlockPos(x, y, z)
					pseudoBlockChanged(nmsChunk, sectionKey, blockPos)
					nmsChunk.level.onBlockStateChange(blockPos, AIR /*TODO hangars */, data)

					section.setBlockState(localX, localY, localZ, data, false)
//					lightModule.`starlight$getLightEngine`().serverLightQueue.queueBlockChange(BlockPos(x, y, z))
				}

//				lightModule.updateSectionStatus(SectionPos.of(chunk.x, sectionKey, chunk.z), false)
			}

			updateHeightMaps(nmsChunk)
			nmsChunk.markUnsaved()
		}

		lightModule.`starlight$serverRelightChunks`(relightChunks, {}, {})

		for ((index, tile) in capturedTiles) {
			val blockKey = newPositionArray[index]
			val x = blockKeyX(blockKey)
			val y = blockKeyY(blockKey)
			val z = blockKeyZ(blockKey)

			val newPos = BlockPos(x, y, z)

			val data = blockDataTransform(tile.first)

			val blockEntity = BlockEntity.loadStatic(newPos, data, tile.second, world2.minecraft.registryAccess()) ?: continue

			val nmsChunk = getNMSChunk(chunkCache, world2, chunkKey(x shr 4, z shr 4))
			nmsChunk.addAndRegisterBlockEntity(blockEntity)
		}
	}

	fun updateHeightMaps(nmsLevelChunk: LevelChunk) {
		Heightmap.primeHeightmaps(nmsLevelChunk, nmsLevelChunk.heightmaps.keys)
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

	private fun getChunkMap(positionArray: LongArray): ChunkMap {
		val chunkMap = Long2ObjectOpenHashMap<Int2ObjectOpenHashMap<Long2IntOpenHashMap>>()

		for (index in positionArray.indices) {
			val blockKey = positionArray[index]
			val x = blockKeyX(blockKey)
			val y = blockKeyY(blockKey)
			val z = blockKeyZ(blockKey)
			val chunkKey = chunkKey(x shr 4, z shr 4)
			val sectionKey = y shr 4
			val sectionMap = chunkMap.getOrPut(chunkKey) { Int2ObjectOpenHashMap() }
			val positionMap = sectionMap.getOrPut(sectionKey) { Long2IntOpenHashMap() }
			positionMap[blockKey] = index
		}

		return chunkMap
	}

	/* Chunk map containing only positions
		from the new chunk map that
		are not in the old chunk map */
	private fun getCollisionChunkMap(oldChunkMap: ChunkMap, newChunkMap: ChunkMap): ChunkMap {
		val chunkMap = Long2ObjectOpenHashMap<Int2ObjectOpenHashMap<Long2IntOpenHashMap>>()

		for ((chunkKey, newSectionMap) in newChunkMap) {
			val oldSectionMap = oldChunkMap[chunkKey]

			for ((sectionKey, newPositionMap) in newSectionMap) {
				val oldPositionMap = oldSectionMap?.get(sectionKey)

				for ((blockKey, index) in newPositionMap) {
					if (oldPositionMap?.containsKey(blockKey) == true) {
						continue
					}

					val sectionMap = chunkMap.getOrPut(chunkKey) { Int2ObjectOpenHashMap() }
					val positionMap = sectionMap.getOrPut(sectionKey) { Long2IntOpenHashMap() }
					positionMap[blockKey] = index
				}
			}
		}

		return chunkMap
	}

	fun sendChunkUpdatesToPlayers(
		world1: World,
		world2: World,
		chunkCache: Long2ObjectOpenHashMap<LevelChunk>,
		oldChunkMap: ChunkMap,
		newChunkMap: ChunkMap,
	) {
		for ((chunkMap, world) in listOf(oldChunkMap to world1.uid, newChunkMap to world2.uid)) {
			for ((chunkKey, _) in chunkMap) {
				val nmsChunk = getNMSChunk(chunkCache, Bukkit.getWorld(world)!!, chunkKey)
				nmsChunk.`moonrise$getChunkAndHolder`().holder.broadcastChanges(nmsChunk)
			}
		}
	}

	private val blocksChangedPersection = ChunkHolder::class.java.getDeclaredField("changedBlocksPerSection").apply { isAccessible = true }
	private val hasChangedSections = ChunkHolder::class.java.getDeclaredField("hasChangedSections").apply { isAccessible = true }

	fun pseudoBlockChanged(chunk: LevelChunk, sectionIndex: Int, blockPos: BlockPos) {
		val holder = chunk.`moonrise$getChunkAndHolder`().holder

		@Suppress("UNCHECKED_CAST") val changedBlockSets: Array<ShortSet?> = blocksChangedPersection.get(holder) as Array<ShortSet?>

		if (changedBlockSets[sectionIndex] == null) {
			hasChangedSections.set(holder, true)
			changedBlockSets[sectionIndex] = ShortOpenHashSet(4096)
		}

		changedBlockSets[sectionIndex]?.add(SectionPos.sectionRelativePos(blockPos))
	}

	private fun getNMSChunk(chunkCache: Long2ObjectOpenHashMap<LevelChunk>, world: World, chunkKey: Long): LevelChunk {
		return chunkCache.getOrPut(chunkKey) { world.getChunkAt(chunkKeyX(chunkKey), chunkKeyZ(chunkKey)).minecraft }
	}
}

private typealias ChunkMap = Map<Long, Map<Int, Map<Long, Int>>>
