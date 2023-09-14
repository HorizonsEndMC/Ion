package net.horizonsend.ion.server.features.space.data

import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.BLOCKS
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.PALETTE
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.Y
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.LevelChunk
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

	fun place(levelChunk: LevelChunk) {
		val worldMin = levelChunk.level.minBuildHeight.shr(4)
		val section = levelChunk.sections[y - worldMin]

		val (chunkX, chunkZ) = levelChunk.pos
		val chunkAbsoluteX = chunkX.shl(4)
		val chunkAbsoluteZ = chunkZ.shl(4)
		val sectionAbsoluteY = section.bottomBlockY()

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
					levelChunk.playerChunk?.blockChanged(blockPos)

					blockData.blockEntityTag?.let {
						val blockEntity = BlockEntity.loadStatic(
							blockPos,
							blockData.blockState,
							it
						) ?: return@let

						levelChunk.addAndRegisterBlockEntity(blockEntity)
					}
				}
			}
		}
	}

	fun setBlock(x: Int, y: Int, z: Int, block: BlockData) =setBlock(posToIndex(x, y ,z), block)

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
}
