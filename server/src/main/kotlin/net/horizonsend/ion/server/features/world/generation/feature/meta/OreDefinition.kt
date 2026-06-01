package net.horizonsend.ion.server.features.world.generation.feature.meta

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.serializer.BlockStateSerializer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import kotlin.math.roundToInt
import kotlin.random.Random

/** A type of ore blob that can be placed in an asteroid */
@Serializable
class OreDefinition(
	val material: BlockType,
	val shape: Int, // Index in ore blob shape array
	val chance: Double
) {
	@Serializable
	sealed interface BlockType {
		fun toBukkitBlockData(): BlockData

		@Serializable
		class CustomBlockType(val key: @Serializable(with = IonRegistryKey.Companion::class) IonRegistryKey<@Contextual CustomBlock, out @Contextual CustomBlock>) : BlockType {
			override fun toBukkitBlockData(): BlockData {
				return key.getValue().blockData
			}
		}

		@Serializable
		class MaterialType(val material: Material) : BlockType {
			override fun toBukkitBlockData(): BlockData {
				return material.createBlockData()
			}
		}

		@Serializable
		class BlockStateType(val state: @Serializable(with = BlockStateSerializer::class) BlockState) : BlockType {
			override fun toBukkitBlockData(): BlockData {
				return state.createCraftBlockData()
			}
		}
	}

	fun getChunkOreCount(metaData: ConfigurableAsteroidMeta): Int {
		val volume = 16 * 16 * 2 * metaData.size
		return (volume * chance).roundToInt()
	}

	fun random(randomSource: Random, start: FeatureStart, metaData: ConfigurableAsteroidMeta, chunkX: Int, chunkZ: Int): OrePlacement {
		val shape = if (shapes.lastIndex <= 0) 0 else randomSource.nextInt(0, shapes.lastIndex)

		val originX = chunkX.shl(4)
		val originZ = chunkZ.shl(4)
		val originY = (start.y - metaData.size).toInt()
		val heightRange = (metaData.size * 2).roundToInt()

		return OrePlacement(
			material.toBukkitBlockData(),
			shape,
			toBlockKey(
				originX + randomSource.nextInt(0, 16),
				originY + randomSource.nextInt(0, heightRange + 1),
				originZ + randomSource.nextInt(0, 16)
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

		fun fromCompound(tag: CompoundTag): OreDefinition {
			return OreDefinition(
				BlockType.BlockStateType(NbtUtils.readBlockState(BuiltInRegistries.BLOCK, tag.getCompound("material").get())),
				tag.getInt("shape").get(),
				tag.getDouble("chance").get()
			)
		}

		fun toCompound(blob: OreDefinition): CompoundTag {
			val tag = CompoundTag()
			tag.put("material", NbtUtils.writeBlockState(blob.material.toBukkitBlockData().nms))
			tag.putInt("shape", blob.shape)
			tag.putDouble("chance", blob.chance)
			return tag
		}
	}

	/** Represents an ore to be placed in the asteroid */
	class OrePlacement(val material: BlockData, val shape: Int, val pos: BlockKey) {
		fun getOffsetCoordinates(): Array<Vec3i> {
			val base = getShape(shape)
			return Array(base.size) { index -> toVec3i(pos).plus(base[index]) }
		}
	}
}
