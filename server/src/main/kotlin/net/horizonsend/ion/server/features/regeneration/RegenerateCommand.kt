package net.horizonsend.ion.server.features.regeneration

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import com.mojang.serialization.DataResult
import com.sk89q.worldedit.WorldEdit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.PalettedContainer
import net.minecraft.world.level.chunk.storage.ChunkSerializer
import net.minecraft.world.level.chunk.storage.RegionFile
import net.starlegacy.util.Tasks
import org.bukkit.World
import org.bukkit.entity.Player
import java.io.File

@CommandPermission("ion.regenerate")
@CommandAlias("regenerate")
class RegenerateCommand : BaseCommand() {
	// The worlds in this folder are stripped down versions of worlds. Basically just renamed region folders
	private val cleanWorldsFolder: File = IonServer.dataFolder.resolve("worlds")
	private val scope = CoroutineScope(Dispatchers.Default)
	private val blockStateCodec = ChunkSerializer.BLOCK_STATE_CODEC

	@Default
	@Suppress("unused")
	fun onRegenerate(sender: Player) {
		val session = WorldEdit.getInstance().sessionManager.findByName(sender.name) ?: return
		val selection = session.getSelection(session.selectionWorld)

		val sections = mutableMapOf<Triple<Int, Int, Int>, CompletableDeferred<Pair<ChunkPos, CompletedSection>>>()
		val sectionsHeight = IntRange(selection.minimumPoint.y.shr(4), selection.maximumPoint.y.shr(4))

		// Group by string first to avoid getting the region dozens of times
		val regionsToChunksMap = selection.chunks.groupBy {
			val regionX = it.x.shr(5)
			val regionZ = it.z.shr(5)

			"r.$regionX.$regionZ.mca"
		}

		for (chunk in selection.chunkCubes) {
			sections[Triple(chunk.x, chunk.y, chunk.z)] = CompletableDeferred()
		}

		for ((regionFile, chunks) in regionsToChunksMap) {
			scope.launch {
				val region = getRegion(sender.world, regionFile)

				for (chunk in chunks) scope.launch chunk@{
					val chunkPos = ChunkPos(chunk.x, chunk.z)

					if (!region.doesChunkExist(chunkPos)) return@chunk

					val chunkData = region.getChunkDataInputStream(chunkPos)?.let { NbtIo.read(it) } ?: return@chunk

					@Suppress("UNCHECKED_CAST")
					val sectionsList = (chunkData.getList("sections", 10).toList() as List<CompoundTag>)
						.associateBy { it.getByte("Y") }

					for (sectionY in sectionsHeight) {
						val storedSection = sectionsList[sectionY.toByte()] ?: continue

						val sectionPos = Triple(chunk.x, sectionY, chunk.z)
						val deferred = sections[sectionPos]!!

						val dataResult = blockStateCodec.parse(NbtOps.INSTANCE, storedSection.getCompound("block_states"))
						val sectionBlocks = (dataResult as DataResult<PalettedContainer<BlockState?>>).getOrThrow(false) {
							IonServer.logger.warning(it)
						}

						regenerateSection(sectionY, chunkPos, sectionBlocks, deferred)
					}
				}
			}
		}

		scope.launch { complete(sender.world, sections.values) }
	}

	private suspend fun complete(world: World, deferredSections: Collection<CompletableDeferred<Pair<ChunkPos, CompletedSection>>>) {
		val sections = deferredSections.awaitAll()

		val chunkMap = sections.groupBy { it.first }.mapKeys { world.minecraft.getChunk(it.key.x, it.key.z) }

		for ((levelChunk, groupedSections) in chunkMap) {
			Tasks.sync {
				for (section in groupedSections) {
					section.second.place(levelChunk)
				}
			}
		}
	}

	private fun regenerateSection(
		sectionY: Int,
		chunkPos: ChunkPos,
		palettedContainer: PalettedContainer<BlockState?>,
		deferred: CompletableDeferred<Pair<ChunkPos, CompletedSection>>
	) {
		val newSection = CompletedSection.empty(sectionY)

		palettedContainer.forEachLocation { blockState: BlockState?, i: Int ->
			if (blockState != null) {
				newSection.setBlock(i, BlockData(blockState, null))
			}
		}

		deferred.complete(chunkPos to newSection)
	}

	private fun getRegion(world: World, regionFileName: String): RegionFile {
		try {
			val region = cleanWorldsFolder.resolve(world.name)

			return RegionFile(region.resolve(regionFileName).toPath(), region.toPath(), false)
		} catch (error: Error) {
			throw error
		}
	}
}
