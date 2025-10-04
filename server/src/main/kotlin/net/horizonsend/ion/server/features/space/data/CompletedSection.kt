package net.horizonsend.ion.server.features.space.data

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.BLOCKS
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.PALETTE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Y
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.generator.ChunkGenerator
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.INTEGER_ARRAY
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER_ARRAY

data class CompletedSection(val y: Int, val palette: MutableList<BlockData>, val blocks: IntArray) {
    companion object : PersistentDataType<PersistentDataContainer, CompletedSection> {
        override fun getPrimitiveType() = PersistentDataContainer::class.java
        override fun getComplexType() = CompletedSection::class.java

        override fun toPrimitive(complex: CompletedSection, context: PersistentDataAdapterContext): PersistentDataContainer {
            val primitive = context.newPersistentDataContainer()

			val paletteArray = complex.palette
				.map { BlockData.toPrimitive(it, context) }
				.toTypedArray()

			primitive.set(Y, INTEGER, complex.y)
			primitive.set(PALETTE, TAG_CONTAINER_ARRAY, paletteArray)
			primitive.set(BLOCKS, INTEGER_ARRAY, complex.blocks)

            return primitive
        }

        override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): CompletedSection {
            val y = primitive.get(Y, INTEGER)!!
			val palette = primitive.get(PALETTE, TAG_CONTAINER_ARRAY)!!.map { BlockData.fromPrimitive(it, context) }
			val blocks = primitive.get(BLOCKS, INTEGER_ARRAY)!!

            return CompletedSection(y, palette as MutableList<BlockData>, blocks)
        }

		fun posToIndex(x: Int, y: Int, z: Int): Int {
			return x or (z shl 4) or (y shl 8)
		}

		fun empty(sectionY: Int) = CompletedSection(sectionY, mutableListOf(BlockData.AIR), IntArray(4096) { 0 })
    }

	fun place(levelChunk: LevelChunk) = Tasks.syncBlocking {
		val worldMin = levelChunk.level.minSectionY

		val section = levelChunk.sections[y - worldMin]

		val chunkAbsoluteX = levelChunk.pos.x.shl(4)
		val chunkAbsoluteZ = levelChunk.pos.z.shl(4)
		val sectionAbsoluteY = (y - worldMin).shl(4)

		for (x in 0..15) {
			val absoluteX = x + chunkAbsoluteX

			for (y in 0..15) {
				val absoluteY = sectionAbsoluteY + y

				for (z in 0..15) {
					val absoluteZ = z + chunkAbsoluteZ

					val index = posToIndex(x, y ,z)
					val paletteIndex = blocks[index]
					val blockData = palette[paletteIndex]

					if (blockData.blockState.isAir) continue

					val blockPos = BlockPos(absoluteX, absoluteY, absoluteZ)

					section.setBlockState(x, y, z, blockData.blockState)
					levelChunk.level.chunkSource.chunkMap.getVisibleChunkIfPresent(levelChunk.pos.toLong())?.blockChanged(blockPos)

					blockData.blockEntityTag?.let {
						val blockEntity = BlockEntity.loadStatic(
							blockPos,
							blockData.blockState,
							it,
							levelChunk.level.registryAccess()
						) ?: return@let

						levelChunk.addAndRegisterBlockEntity(blockEntity)
					}
				}
			}
		}
	}

	fun place(chunkData: ChunkGenerator.ChunkData, nms: ChunkAccess, x: Int, z: Int) {
		val worldMin = chunkData.minHeight.shr(4)

		val chunkAbsoluteX = x.shl(4)
		val chunkAbsoluteZ = z.shl(4)
		val sectionAbsoluteY = (y - worldMin).shl(4)

		for (x in 0..15) {
			val absoluteX = x + chunkAbsoluteX

			for (y in 0..15) {
				val absoluteY = sectionAbsoluteY + y

				for (z in 0..15) {
					val absoluteZ = z + chunkAbsoluteZ

					val index = posToIndex(x, y ,z)
					val paletteIndex = blocks[index]
					val blockData = palette[paletteIndex]

					if (blockData.blockState.isAir) continue

					val blockPos = BlockPos(absoluteX, absoluteY, absoluteZ)

					chunkData.setBlock(x, absoluteY, z, blockData.blockState.createCraftBlockData())

					if (nms !is LevelChunk) continue

					blockData.blockEntityTag?.let {
						val blockEntity = BlockEntity.loadStatic(
							blockPos,
							blockData.blockState,
							it,
							nms.level.registryAccess()
						) ?: return@let

						nms.addAndRegisterBlockEntity(blockEntity)
					}
				}
			}
		}
	}

	fun setBlock(x: Int, y: Int, z: Int, block: BlockData) = setBlock(posToIndex(x, y ,z), block)

	fun setBlock(index: Int, block: BlockData) {
		if (palette.contains(block)) {
			blocks[index] = palette.indexOf(block)
		} else {
			palette.add(block)
			blocks[index] = palette.lastIndex
		}
	}

	fun getBlock(x: Int, y: Int, z: Int): BlockData {
		val index = posToIndex(x, y ,z)

		return palette[blocks[index]]
	}

	fun isEmpty(): Boolean = blocks.all { it == 0 }

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as CompletedSection

		if (y != other.y) return false
		if (palette != other.palette) return false
		return blocks.contentEquals(other.blocks)
	}

	override fun hashCode(): Int {
		var result = y
		result = 31 * result + palette.hashCode()
		result = 31 * result + blocks.contentHashCode()
		return result
	}
}
