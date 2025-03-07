package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import com.github.auburn.FastNoiseLite
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.OreBlob
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Add
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.IterativeValueProvider
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Max
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Multiply
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.NoiseConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Size
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Static
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Subtract
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Sum
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.util.noise.PerlinOctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import kotlin.math.sqrt
import kotlin.random.Random

private val standardLayers: IterativeValueProvider = Sum(listOf(
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
			amplitude = 100.0
		).build(),
		NoiseConfiguration(
			noiseTypeConfiguration = NoiseConfiguration.NoiseTypeConfiguration.OpenSimplex2(
				featureSize = 25f,
			),
			fractalSettings = NoiseConfiguration.FractalSettings.None,
			domainWarpConfiguration = NoiseConfiguration.DomainWarpConfiguration.None,
			amplitude = 15.0
		).build(),
		NoiseConfiguration(
			NoiseConfiguration.NoiseTypeConfiguration.Voronoi(
				featureSize = 10f,
				distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
				returnType = FastNoiseLite.CellularReturnType.Distance,
			),
			NoiseConfiguration.FractalSettings.None,
			NoiseConfiguration.DomainWarpConfiguration.None,
			10.0,
			normalizedPositive = false
		).build()
	))

private val coronavirus: IterativeValueProvider = Add(
		Multiply(
		Size,
		Static(0.75)
		),
		Sum(listOf(
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
				amplitude = 20.0,
				normalizedPositive = false
			).build(),
			NoiseConfiguration(
				noiseTypeConfiguration = NoiseConfiguration.NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 75f,
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
				amplitude = 20.0,
				normalizedPositive = false
			).build(),
			NoiseConfiguration(
				NoiseConfiguration.NoiseTypeConfiguration.Voronoi(
					featureSize = 30f,
					distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
					returnType = FastNoiseLite.CellularReturnType.Distance,
				),
				NoiseConfiguration.FractalSettings.None,
				NoiseConfiguration.DomainWarpConfiguration.None,
				20.0,
				normalizedPositive = false
			).build()
		))
	)

class ConfigurableAsteroidMeta(
	override val seed: Long,
	val size: Double,
	val block: Material,
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
	private val noiseLayers: IterativeValueProvider =
		Add(
			Multiply(a = Size, b = Static(0.65)),
			Multiply(
				a = Multiply(Size, Static(0.35)),
				b = Subtract(
					a = Max(
						a = NoiseConfiguration(
							noiseTypeConfiguration = NoiseConfiguration.NoiseTypeConfiguration.Perlin(featureSize = 10f),
							fractalSettings = NoiseConfiguration.FractalSettings.None,
							domainWarpConfiguration = NoiseConfiguration.DomainWarpConfiguration.None,
							amplitude = 1.0,
							normalizedPositive = true
						).build(),
						b = Static(0.75)
					),
					b = Static(0.75)
				)
			)
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
		return noiseLayers.getValue(x, y, z, this) * normalizingFactor
	}

	private fun getMaxDisplacement(): Double {
		return noiseLayers.getFallbackValue(this)
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
