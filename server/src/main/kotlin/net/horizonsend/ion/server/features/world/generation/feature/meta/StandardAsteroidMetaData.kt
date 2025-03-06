package net.horizonsend.ion.server.features.world.generation.feature.meta

import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.util.noise.PerlinOctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class StandardAsteroidMetaData(
	override val seed: Long,
	val size: Double,
	val block: Material,
//	val paletteBlocks: List<BlockState> = listOf(
//		Material.QUARTZ_BRICKS.createBlockData().nms,
//		Material.QUARTZ_BLOCK.createBlockData().nms,
//		Material.DIORITE.createBlockData().nms,
//		Material.POLISHED_DIORITE.createBlockData().nms,
//		Material.RED_GLAZED_TERRACOTTA.createBlockData().nms,
//		Material.NETHERRACK.createBlockData().nms,
//		Material.RED_NETHER_BRICKS.createBlockData().nms,
//	),
	val paletteBlocks: List<BlockState> = listOf(
		Material.BLUE_GLAZED_TERRACOTTA,
		Material.TUBE_CORAL_BLOCK,
		Material.LIGHT_BLUE_TERRACOTTA,
		Material.LIGHT_BLUE_CONCRETE,
		Material.BLUE_ICE,
		Material.PACKED_ICE,
//		Material.ICE,
		Material.LIGHT_BLUE_WOOL,
		Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
		Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
		Material.DIORITE,
		Material.CALCITE,
		Material.SNOW_BLOCK,
	).map { it.createBlockData().nms },
	val oreBlobs: MutableList<OreBlob> = mutableListOf(),
	val octaves: Int = 3,
) : FeatureMetaData {
	val sizeSquared = size.squared()
	override val factory: FeatureMetadataFactory<StandardAsteroidMetaData> = Factory
	private val sizeFactor = size / 15

	val random = Random(seed)
	val cave1 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }
	val cave2 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }

	private val initialScale = 0.015 / sizeFactor.coerceAtLeast(1.0)
	val shapingNoise: Array<SimplexOctaveGenerator> = Array(octaves) { octave ->
		val noiseLayer = SimplexOctaveGenerator(seed, 1)
		noiseLayer.setScale(initialScale * (octave + 1.0).pow(2.25 + (sizeFactor / 2.25).coerceAtMost(0.5)))
		noiseLayer
	}

	val totalDisplacement = (0..octaves).sumOf { octave -> scaleNoiseFactor(octave) }
	val normalizingFactor = size / totalDisplacement

	init {
	    println("total displacement: $totalDisplacement, size: $size")
	}

	val materialNoise = SimplexOctaveGenerator(seed, 1).apply {
		this.setScale(0.15 / sqrt(sizeFactor))
	}

	fun scaleNoiseFactor(octave: Int): Double {
		return (size / (octave + 1))
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
