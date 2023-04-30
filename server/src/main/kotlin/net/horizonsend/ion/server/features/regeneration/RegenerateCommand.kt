package net.horizonsend.ion.server.features.regeneration

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import com.mojang.serialization.DataResult
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.math.BlockVector3
import net.horizonsend.ion.server.IonServer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.PalettedContainer
import net.minecraft.world.level.chunk.storage.ChunkSerializer
import net.minecraft.world.level.chunk.storage.RegionFile
import net.starlegacy.util.setNMSBlockData
import org.bukkit.World
import org.bukkit.entity.Player
import java.io.File

@CommandPermission("ion.regenerate")
@CommandAlias("regenerate")
class RegenerateCommand : BaseCommand() {
	// The worlds in this folder are stripped down versions of worlds. Basically just renamed region folders
	private val cleanWorldsFolder: File = IonServer.dataFolder.resolve("worlds")

	@Default
	@Suppress("unused")
	fun onRegenerate(sender: Player) {
		val session = WorldEdit.getInstance().sessionManager.findByName(sender.name) ?: return
		val selection = session.getSelection(session.selectionWorld)

		val regionsToChunksMap = selection.chunks.groupBy {
			val regionX = it.x.shr(5)
			val regionZ = it.z.shr(5)

			"r.$regionX.$regionZ.mca"
		}

		val regionFiles = regionsToChunksMap.keys.associateWith { getRegion(sender.world, it) }

		val sectionPositionsToData = mutableMapOf<BlockVector3, PalettedContainer<BlockState?>>()

		val sections = IntRange(selection.minimumPoint.y.shr(4), selection.maximumPoint.y.shr(4))

		for ((regionFileName, chunks) in regionsToChunksMap) {
			val regionFile = regionFiles[regionFileName]!!

			for (chunk in chunks) {
				val chunkPos = ChunkPos(chunk.x, chunk.z)
				if (!regionFile.doesChunkExist(chunkPos)) continue

				val chunkData = regionFile.getChunkDataInputStream(chunkPos)?.let { NbtIo.read(it) } ?: continue
				val sectionsList = chunkData.getList("sections", 10).toList()

				for (section in sections) {
					val sectionData = sectionsList[section] as CompoundTag
					val blockStateCodec = ChunkSerializer.BLOCK_STATE_CODEC

					val dataResult = blockStateCodec.parse(NbtOps.INSTANCE, sectionData.getCompound("block_states"))
					val sectionBlocks = (dataResult as DataResult<PalettedContainer<BlockState?>>).getOrThrow(false) {
						IonServer.logger.warning(it)
					}

					val pos = BlockVector3.at(chunkPos.x, section, chunkPos.z)

					sectionPositionsToData[pos] = sectionBlocks
				}
			}
		}

		for (blockVector3 in selection) {
			val chunkX = blockVector3.x.shr(4)
			val chunkY = (blockVector3.y - sender.world.minHeight).shr(4)
			val chunkZ = blockVector3.z.shr(4)

			val x = blockVector3.x
			val y = blockVector3.y
			val z = blockVector3.z

			val localX = x - chunkX.shl(4)
			val localY = y - chunkY.shl(4)
			val localZ = z - chunkZ.shl(4)

			val data = sectionPositionsToData[BlockVector3.at(chunkX, chunkY, chunkZ)]!!

			val blockState = data[localX, localY, localZ] ?: continue

			sender.world.setNMSBlockData(blockVector3.x, blockVector3.y, blockVector3.z, blockState)
		}
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
