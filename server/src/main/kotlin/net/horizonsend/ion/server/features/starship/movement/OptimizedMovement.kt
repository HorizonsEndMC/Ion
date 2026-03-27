package net.horizonsend.ion.server.features.starship.movement

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet
import it.unimi.dsi.fastutil.shorts.ShortSet
import net.horizonsend.ion.server.features.starship.BlockingBypass
import net.horizonsend.ion.server.features.starship.Hangars
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
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

/**
 * High-performance starship block movement pipeline.
 *
 * This utility moves large sets of blocks by working directly with chunk sections
 * and NMS block state data instead of using Bukkit's normal per-block mutation
 * APIs. The movement flow is:
 *
 * 1. Group source and destination positions by chunk section.
 * 2. Check destination-only positions for collisions.
 * 3. Capture source block states and block entities.
 * 4. Remove source blocks.
 * 5. Dissipate any hangar-like obstructing blocks in the destination.
 * 6. Place transformed block states at their new positions.
 * 7. Restore moved block entities.
 * 8. Relight affected chunks and broadcast section deltas to players.
 *
 * This class is intentionally low-level and version-sensitive. It relies on NMS
 * chunk internals and manual chunk-holder dirty tracking in order to avoid the
 * cost of ordinary block placement for large starship moves.
 *
 * Version note:
 * This implementation depends on Paper/NMS chunk internals and may require
 * maintenance when Mojang mappings or Paper chunk-holder internals change.
 */
object OptimizedMovement {
	/**
	 * Block states that are ignored during collision checks.
	 *
	 * These are destination contents that a moving starship may safely overwrite
	 * without being considered blocked.
	 */
	private val passThroughBlocks = listOf(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.SNOW).mapTo(ObjectOpenHashSet()) { it.createBlockData().nms }

	/**
	 * Moves a starship's blocks from one position set to another.
	 *
	 * The caller supplies matching source and destination position arrays where each
	 * index represents one moved block. The source block state at index `i` is read,
	 * optionally transformed, and placed at destination index `i`.
	 *
	 * This method performs the move synchronously on the main thread through
	 * [Tasks.syncBlocking], because chunk mutation, lighting, and block entity
	 * registration must occur in the live world thread.
	 *
	 * The move pipeline is:
	 * - build source, destination, and collision chunk maps
	 * - validate destination collisions
	 * - capture and remove source blocks
	 * - dissipate hangar-like destination blocks
	 * - place transformed destination blocks
	 * - invoke the supplied callback
	 * - broadcast the recorded chunk changes to players
	 *
	 * @param executionCheck guard used to abort the move immediately before mutation
	 * @param currentWorld world containing the source blocks
	 * @param newWorld world containing the destination blocks
	 * @param oldPositionArray packed source block positions
	 * @param newPositionArray packed destination block positions
	 * @param blockStateTransform transformation applied to each captured source block state before placement
	 * @param chunkCache reusable chunk cache for repeated NMS chunk lookups
	 * @param callback invoked after block placement and before client update broadcasting
	 *
	 * @throws StarshipBlockedException if a destination block cannot be overwritten
	 */
	fun moveStarship(
		executionCheck: () -> Boolean,
		currentWorld: World,
		newWorld: World,
		oldPositionArray: LongArray,
		newPositionArray: LongArray,
		blockStateTransform: (BlockState) -> BlockState,
		chunkCache: ChunkCache = Object2ObjectOpenHashMap(),
		callback: () -> Unit
	) {
		val oldChunkMap = getChunkMap(oldPositionArray)
		val newChunkMap = getChunkMap(newPositionArray)
		val collisionChunkMap = getCollisionChunkMap(oldChunkMap, newChunkMap)

		val n = oldPositionArray.size
		val capturedStates = java.lang.reflect.Array.newInstance(BlockState::class.java, n) as Array<BlockState>
		val capturedTiles = mutableMapOf<Int, Pair<BlockState, CompoundTag>>()
		val hangars = LinkedList<Long>()

		try {
			Tasks.syncBlocking {
				if (!executionCheck.invoke()) {
					return@syncBlocking
				}

				checkForCollision(newWorld, collisionChunkMap, chunkCache, hangars, oldPositionArray, newPositionArray)

				processOldBlocks(
					oldChunkMap,
					currentWorld,
					chunkCache,
					capturedStates,
					capturedTiles
				)

				dissipateHangarBlocks(newWorld, hangars)

				processNewBlocks(
					newPositionArray,
					newChunkMap,
					newWorld,
					chunkCache,
					capturedStates,
					capturedTiles,
					blockStateTransform
				)

				callback()

				sendChunkUpdatesToPlayers(currentWorld, newWorld, chunkCache, oldChunkMap, newChunkMap)
			}
		} catch (e: ExecutionException) {
			throw e.cause ?: e
		}
	}

	/**
	 * Validates destination positions that are not already occupied by the moving ship.
	 *
	 * Only positions present in the destination map but absent from the source map are
	 * checked here. If a destination block is not pass-through, not a dissipatable
	 * hangar block, and not allowed by [BlockingBypass], the move is aborted with a
	 * [StarshipBlockedException].
	 *
	 * Hangar-like blocks that can be cleared are collected into [hangars] for later
	 * dissipation after the source blocks have been removed.
	 */
	private fun checkForCollision(
		world: World,
		collisionChunkMap: ChunkMap,
		chunkCache: ChunkCache,
		hangars: LinkedList<Long>,
		oldPositionArray: LongArray,
		newPositionArray: LongArray
	) {
		for ((chunkKey, sectionMap) in collisionChunkMap) {
			val nmsChunk = getNMSChunk(world, chunkKey, chunkCache)

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
						if (!isHangar(blockData) && !BlockingBypass.objectIsSmallEnough(oldPositionArray, blockKey, world)) {
							throw StarshipBlockedException(Vec3i(x, y, z), blockData)
						}

						hangars.add(blockKey)
					}
				}
			}
		}
	}

	/**
	 * Returns whether a destination block should be treated as a dissipatable hangar-style obstruction
	 * rather than a hard collision.
	 *
	 * These are generally fragile or soft world blocks that starships are allowed to
	 * clear during movement instead of treating them as blocking terrain.
	 */
	private fun isHangar(newBlockData: BlockState) =
		newBlockData.block is StainedGlassBlock
		|| newBlockData.block is NetherPortalBlock
		|| newBlockData.block is LiquidBlock
		|| newBlockData.block is BushBlock // most types of crop/grass blocks
		|| newBlockData.block is VineBlock // normal vines
		|| newBlockData.block is GrowingPlantBlock // twisted vines on Luxiterna, kelp, etc.
		|| newBlockData.block is LeavesBlock
		|| newBlockData.block is BaseCoralPlantTypeBlock
		|| newBlockData.block is BambooSaplingBlock
		|| newBlockData.block is BambooStalkBlock
		|| newBlockData.block is FungusBlock
		|| newBlockData.block is DoublePlantBlock
		|| newBlockData.block is GlowLichenBlock

	/**
	 * Removes hangar-style destination obstructions collected during collision checks.
	 *
	 * This is performed after source blocks have been captured and removed, but before
	 * the moved blocks are placed into their destination positions.
	 */
	private fun dissipateHangarBlocks(newWorld: World, hangars: LinkedList<Long>) {
		for (blockKey in hangars.iterator()) {
			Hangars.dissipateBlock(newWorld, blockKey)
		}
	}

	/**
	 * Shared NMS air state used when clearing source positions.
	 */
	val AIR: BlockState = Blocks.AIR.defaultBlockState()

	/**
	 * Captures and removes all source blocks from their original positions.
	 *
	 * For each source position this method:
	 * - reads the current block state into [capturedStates]
	 * - captures block entity data when the block owns a tile entity
	 * - marks the block position as changed for later chunk-holder broadcasting
	 * - emits a world block-update notification
	 * - writes air directly into the backing chunk section
	 *
	 * After section edits are complete, the affected chunks are re-primed for
	 * heightmaps, marked unsaved, and queued for relighting.
	 *
	 * This method intentionally edits chunk sections directly for speed instead of
	 * using ordinary Bukkit block placement.
	 */
	private fun processOldBlocks(
		oldChunkMap: ChunkMap,
		currentWorld: World,
		chunkCache: ChunkCache,
		capturedStates: Array<BlockState>,
		capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>
	) {
		val lightModule = currentWorld.minecraft.lightEngine
		val relightChunks = ObjectOpenHashSet<ChunkPos>()

		for ((chunkKey, sectionMap) in oldChunkMap) {
			val nmsChunk = getNMSChunk(currentWorld, chunkKey, chunkCache)
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
					//TODO: PICK OUT FLAGS
					nmsChunk.level.sendBlockUpdated(blockPos, type, AIR, 0)

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

	/**
	 * Places the moved blocks into their destination positions and restores block entities.
	 *
	 * For each destination position this method:
	 * - takes the captured source block state at the same array index
	 * - applies [blockDataTransform]
	 * - marks the destination position as changed for later chunk-holder broadcasting
	 * - emits a world block-update notification
	 * - writes the transformed state directly into the destination chunk section
	 *
	 * After block placement, affected chunks are re-primed for heightmaps, marked
	 * unsaved, relit, and any captured block entities are reloaded and registered
	 * at their new positions.
	 */
	private fun processNewBlocks(
		newPositionArray: LongArray,
		newChunkMap: ChunkMap,
		newWorld: World,
		chunkCache: ChunkCache,
		capturedStates: Array<BlockState>,
		capturedTiles: MutableMap<Int, Pair<BlockState, CompoundTag>>,
		blockDataTransform: (BlockState) -> BlockState
	) {
		val lightModule = newWorld.minecraft.lightEngine
		val relightChunks = ObjectOpenHashSet<ChunkPos>()

		for ((chunkKey, sectionMap) in newChunkMap) {
			val nmsChunk = getNMSChunk(newWorld, chunkKey, chunkCache)
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
					//TODO: PICK OUT FLAGS
					nmsChunk.level.sendBlockUpdated(blockPos, AIR /*TODO hangars */, data, 0)

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
			val chunk = newWorld.getChunkAt(x shr 4, z shr 4)

			val data = blockDataTransform(tile.first)

			val blockEntity = BlockEntity.loadStatic(newPos, data, tile.second, newWorld.minecraft.registryAccess()) ?: continue
			chunk.minecraft.addAndRegisterBlockEntity(blockEntity)
		}
	}

	/**
	 * Rebuilds all existing heightmaps for a chunk after direct section edits.
	 *
	 * This is required because OptimizedMovement bypasses the normal block placement
	 * pipeline and therefore must refresh chunk-derived height data manually.
	 */
	fun updateHeightMaps(nmsLevelChunk: LevelChunk) {
		Heightmap.primeHeightmaps(nmsLevelChunk, nmsLevelChunk.heightmaps.keys)
	}

	/**
	 * Captures block entity state for a moved source block and removes the original block entity.
	 *
	 * The saved pair contains:
	 * - the block state originally associated with the block entity
	 * - the full serialized NBT required to recreate it later
	 *
	 * Captured entries are keyed by the movement array index so they can be restored
	 * at the matching destination position after block placement.
	 */
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

	/**
	 * Groups packed block positions into a three-level lookup:
	 *
	 * chunk key -> section Y -> block key -> original array index
	 *
	 * This layout allows movement code to iterate block edits in chunk/section order,
	 * which is substantially more efficient than random per-block world access.
	 *
	 * The stored array index preserves the positional correspondence between
	 * [oldPositionArray], [newPositionArray], [capturedStates], and [capturedTiles].
	 */
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

	/**
	 * Builds a chunk map containing only destination positions that are not already
	 * occupied by the moving ship's current source positions.
	 *
	 * These are the only positions that require collision checks, because blocks that
	 * are simply moving from one owned position to another do not count as external
	 * obstructions.
	 */
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

	/**
	 * Broadcasts recorded block changes for all affected chunks to nearby players.
	 *
	 * OptimizedMovement writes directly into chunk sections, so it cannot rely on the
	 * normal per-block server update pipeline to accumulate and flush client deltas.
	 * Instead, changed positions are recorded manually through [pseudoBlockChanged],
	 * then each affected chunk holder is asked to broadcast its accumulated changes.
	 *
	 * Both the source-world and destination-world chunk maps are broadcast because a
	 * move may affect different chunks in each world.
	 */
	fun sendChunkUpdatesToPlayers(
		currentWorld: World,
		newWorld: World,
		chunkCache: ChunkCache,
		oldChunkMap: ChunkMap,
		newChunkMap: ChunkMap,
	) {
		for ((chunkMap, world) in listOf(oldChunkMap to currentWorld.uid, newChunkMap to newWorld.uid)) {
			for ((chunkKey, _) in chunkMap) {
				val nmsChunk = getNMSChunk(Bukkit.getWorld(world)!!, chunkKey, chunkCache)
				nmsChunk.`moonrise$getChunkHolder`().vanillaChunkHolder.broadcastChanges(nmsChunk)
			}
		}
	}

	private val blocksChangedPersection = ChunkHolder::class.java.getDeclaredField("changedBlocksPerSection").apply { isAccessible = true }
	private val hasChangedSections = ChunkHolder::class.java.getDeclaredField("hasChangedSections").apply { isAccessible = true }

	/**
	 * Manually records a changed block position into the chunk holder's per-section
	 * delta sets so [ChunkHolder.broadcastChanges] will send the change to players.
	 *
	 * This exists because OptimizedMovement mutates chunk sections directly via
	 * [net.minecraft.world.level.chunk.LevelChunkSection.setBlockState] instead of
	 * going through the normal server block mutation pipeline. Without this manual
	 * dirty tracking, later chunk broadcasts may miss the changed blocks.
	 *
	 * @param chunk the owning chunk
	 * @param sectionIndex section Y index within the chunk
	 * @param blockPos absolute world block position that changed
	 */
	fun pseudoBlockChanged(chunk: LevelChunk, sectionIndex: Int, blockPos: BlockPos) {
		val holder = chunk.`moonrise$getChunkHolder`().vanillaChunkHolder

		@Suppress("UNCHECKED_CAST") val changedBlockSets: Array<ShortSet?> = blocksChangedPersection.get(holder) as Array<ShortSet?>

		if (changedBlockSets[sectionIndex] == null) {
			hasChangedSections.set(holder, true)
			changedBlockSets[sectionIndex] = ShortOpenHashSet(4096)
		}

		changedBlockSets[sectionIndex]?.add(SectionPos.sectionRelativePos(blockPos))
	}

	/**
	 * Returns a cached NMS chunk for the given world and packed chunk key.
	 *
	 * Chunk lookups are memoized per-world for the duration of a movement operation
	 * to reduce repeated Bukkit-to-NMS conversion costs.
	 */
	private fun getNMSChunk(world: World, chunkKey: Long, chunkCache: ChunkCache): LevelChunk {
		val worldCaches = chunkCache.getOrPut(world) { Long2ObjectOpenHashMap<LevelChunk>() }
		return worldCaches.getOrPut(chunkKey) { world.getChunkAt(chunkKeyX(chunkKey), chunkKeyZ(chunkKey)).minecraft }
	}
}

/**
 * Grouped block positions keyed by chunk and then by vertical section.
 *
 * Structure:
 * chunk key -> section Y -> packed block key -> movement array index
 */
private typealias ChunkMap = Map<Long, Map<Int, Map<Long, Int>>>
/**
 * Per-world cache of resolved NMS chunks used during one movement operation.
 */
private typealias ChunkCache = MutableMap<World, Long2ObjectOpenHashMap<LevelChunk>>
