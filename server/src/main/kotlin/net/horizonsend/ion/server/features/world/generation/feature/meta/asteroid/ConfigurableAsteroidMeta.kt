package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import com.github.auburn.FastNoiseLite
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.OreBlob
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Add
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.BlockPlacementConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.BlockPlacer
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.IterativeValueProvider
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Multiply
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.NoiseConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Size
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Static
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.Sum
import net.minecraft.nbt.CompoundTag
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
	val oreBlobs: MutableList<OreBlob> = mutableListOf(),
	private val noiseLayers: IterativeValueProvider = Sum(listOf(
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
	)),
	val blockPlacer: BlockPlacer = BlockPlacementConfiguration(
		blocks = listOf(
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.BLUE_GLAZED_TERRACOTTA, 3.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.TUBE_CORAL_BLOCK, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.LIGHT_BLUE_TERRACOTTA, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.LIGHT_BLUE_CONCRETE, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.BLUE_ICE, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.PACKED_ICE, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.ICE, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.LIGHT_BLUE_WOOL, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.CLAY, 2.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.STONE, 17.5),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.CLAY, 2.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.DIORITE, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.CALCITE, 1.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.SNOW_BLOCK, 3.0),
			BlockPlacementConfiguration.PlacedBlockConfiguration(Material.DIORITE, 7.0),
		)
	).build()
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
