package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import com.github.auburn.FastNoiseLite
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.OreBlob
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.AsteroidConfiguration.StartValue
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.util.noise.PerlinOctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import kotlin.math.sqrt
import kotlin.random.Random

class ConfigurableAsteroidMeta(
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
	val startValue: StartValue = StartValue.NONE,
	private val noiseLayers: List<NoiseWrapper> = listOf(
		NoiseConfiguration(
			noiseTypeConfiguration = NoiseConfiguration.NoiseTypeConfiguration.OpenSimplex2(
				featureSize = 150f,
			),
			fractalSettings = NoiseConfiguration.FractalSettings.FractalParameters(
				type = NoiseConfiguration.FractalSettings.NoiseFractalType.FBM,
				octaves = 3,
				lunacrity = 2f,
				gain = 1f,
				weightedStrength = 3f,
				pingPongStrength = 1f
			),
			domainWarpConfiguration = NoiseConfiguration.DomainWarpConfiguration.None,
			blendMode = BlendMode.ADD,
			amplitude = 100.0f
		).build(),
		NoiseConfiguration(
			noiseTypeConfiguration = NoiseConfiguration.NoiseTypeConfiguration.OpenSimplex2(
				featureSize = 25f,
			),
			fractalSettings = NoiseConfiguration.FractalSettings.None,
			domainWarpConfiguration = NoiseConfiguration.DomainWarpConfiguration.DomainWarpParameters(
				domainWarpType = FastNoiseLite.DomainWarpType.OpenSimplex2,
				rotationType3D = FastNoiseLite.RotationType3D.None,
				amplitude = 91f,
				noiseType = NoiseConfiguration.NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 20f,
				),
				fractalSettings = NoiseConfiguration.FractalSettings.None
			),
			blendMode = BlendMode.ADD,
			amplitude = 15.0f
		).build(),
		NoiseConfiguration(
			NoiseConfiguration.NoiseTypeConfiguration.Voronoi(
				featureSize = 10f,
				distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
				returnType = FastNoiseLite.CellularReturnType.Distance,
			),
			NoiseConfiguration.FractalSettings.None,
			NoiseConfiguration.DomainWarpConfiguration.None,
			BlendMode.ADD,
			10.0f,
			normalizedPositive = false
		).build()
	)
) : FeatureMetaData {
	override val factory: FeatureMetadataFactory<ConfigurableAsteroidMeta> = Factory

	val sizeSquared = size.squared()
	val random = Random(seed)

	val cave1 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }
	val cave2 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }

	val materialNoise = SimplexOctaveGenerator(seed, 1).apply {
		this.setScale(0.15 / sqrt(size / 15))
	}

	val totalDisplacement = getMaxDisplacement()
	private val normalizingFactor = size / totalDisplacement

	fun getNoise(x: Double, y: Double, z: Double): Double {
		var total = startValue.getValue(this)

		for (noiseLayer in noiseLayers) {
			val noise = noiseLayer.getNoise(x, y, z)
			total = noiseLayer.blendMode.apply(total.toFloat(), noise).toDouble()
		}

		return total * normalizingFactor
	}

	private fun getMaxDisplacement(): Double {
		var total = startValue.getValue(this)

		for (noiseLayer in noiseLayers) {
			total = noiseLayer.blendMode.apply(total.toFloat(), 1.0f * noiseLayer.amplitude).toDouble()
		}

		return total
	}

	object Factory : FeatureMetadataFactory<ConfigurableAsteroidMeta>() {
		override fun load(data: CompoundTag): ConfigurableAsteroidMeta {
			return ConfigurableAsteroidMeta(
				data.getLong("seed"),
				data.getDouble("size"),
				Material.valueOf(data.getString("block"))
			)
		}

		override fun saveData(featureData: ConfigurableAsteroidMeta): CompoundTag {
			val tag = CompoundTag()
			tag.putLong("seed", featureData.seed)
			tag.putDouble("size", featureData.size)
			tag.putString("block", featureData.block.name)
			return tag
		}
	}
}
