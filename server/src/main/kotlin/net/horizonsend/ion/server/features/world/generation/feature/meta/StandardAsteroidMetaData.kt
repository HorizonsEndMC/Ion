package net.horizonsend.ion.server.features.world.generation.feature.meta

import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.util.noise.PerlinOctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import kotlin.math.sqrt
import kotlin.random.Random

class StandardAsteroidMetaData(
	override val seed: Long,
	val size: Double,
	val block: Material,
	val paletteBlocks: List<BlockState> = listOf(
		Material.QUARTZ_BRICKS.createBlockData().nms,
		Material.QUARTZ_BLOCK.createBlockData().nms,
		Material.DIORITE.createBlockData().nms,
		Material.POLISHED_DIORITE.createBlockData().nms,
		Material.RED_GLAZED_TERRACOTTA.createBlockData().nms,
		Material.NETHERRACK.createBlockData().nms,
		Material.RED_NETHER_BRICKS.createBlockData().nms,
	),
	val oreBlobs: MutableList<OreBlob> = mutableListOf(),
	val octaves: Int = 2,
) : FeatureMetaData {
	override val factory: FeatureMetadataFactory<StandardAsteroidMetaData> = Factory
	val sizeFactor = size / 15

	val random = Random(seed)
	val cave1 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }
	val cave2 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }

	val shapingNoise = SimplexOctaveGenerator(seed, 1)
	val materialNoise = SimplexOctaveGenerator(seed, 1).apply {
		this.setScale(0.15 / sqrt(sizeFactor))
	}

	object Factory : FeatureMetadataFactory<StandardAsteroidMetaData>() {
		override fun load(data: CompoundTag): StandardAsteroidMetaData {
			return StandardAsteroidMetaData(
				data.getLong("seed"),
				data.getDouble("size"),
				Material.valueOf(data.getString("block"))
			)
		}

		override fun saveData(featureData: StandardAsteroidMetaData): CompoundTag {
			val tag = CompoundTag()
			tag.putLong("seed", featureData.seed)
			tag.putDouble("size", featureData.size)
			tag.putString("block", featureData.block.name)
			return tag
		}
	}
}
