package net.horizonsend.ion.server.features.world.generation.feature.meta

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.serializer.BlockStateSerializer
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.roundToInt

/** A type of ore blob that can be placed in an asteroid */
@Serializable
class OreDefinition(
	val material: @Serializable(with = BlockStateSerializer::class) BlockState,
	val shape: Int, // Index in ore blob shape array
	val chance: Double
) {
	fun getChunkOreCount(metaData: ConfigurableAsteroidMeta): Int {
		val volume = 16 * 16 * 2 * metaData.size
		return (volume * chance).roundToInt()
	}

	fun random(randomSource: RandomSource, start: FeatureStart, metaData: ConfigurableAsteroidMeta, chunkX: Int, chunkZ: Int): OrePlacement {
		val shape = if (shapes.lastIndex <= 0) 0 else randomSource.nextInt(0, shapes.lastIndex)

		val originX = chunkX.shl(4)
		val originZ = chunkZ.shl(4)
		val originY = (start.y - metaData.size).toInt()
		val heightRange = (metaData.size * 2).roundToInt()

		return OrePlacement(
			material,
			shape,
			toBlockKey(
				originX + randomSource.nextInt(0, 15),
				originY + randomSource.nextInt(0, heightRange),
				originZ + randomSource.nextInt(0, 15)
			),
		)
	}

	companion object {
		/**
		 * List of shape arrays for ore blobs
		 *
		 **/
		private val shapes = mutableListOf<Array<Vec3i>>(
			/* 2x2 */ arrayOf(Vec3i(0, 0, 0), Vec3i(0, 0, 1), Vec3i(1, 0, 0), Vec3i(1, 0, 1), Vec3i(0, 1, 0), Vec3i(0, 1, 1), Vec3i(1, 1, 0), Vec3i(1, 1, 1))
		)

		fun getShape(index: Int): Array<Vec3i> = shapes[index]

		fun getShapeCount(): Int = shapes.size
	}

	/** Represents an ore to be placed in the asteroid */
	class OrePlacement(val material: BlockState, val shape: Int, val pos: BlockKey) {
		fun getOffsetCoordinates(): Array<Vec3i> {
			val base = getShape(shape)
			return Array(base.size) { index -> toVec3i(pos).plus(base[index]) }
		}

		fun fromCompound(tag: CompoundTag): OrePlacement {
			return OrePlacement(
				NbtUtils.readBlockState(BuiltInRegistries.BLOCK, tag.getCompound("material").get()),
				tag.getInt("shape").get(),
				tag.getLong("pos").get()
			)
		}

		fun toCompound(blob: OrePlacement): CompoundTag {
			val tag = CompoundTag()
			tag.put("material", NbtUtils.writeBlockState(blob.material))
			tag.putInt("shape", shape)
			tag.putLong("pos", pos)
			return tag
		}
	}
}
