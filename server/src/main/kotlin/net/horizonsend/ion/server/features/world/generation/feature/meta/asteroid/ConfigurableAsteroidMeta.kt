package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import com.github.auburn.FastNoiseLite
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.OreBlob
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material.MaterialConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material.NoiseMaterialConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material.SimpleMaterialConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material.SurfaceDistanceAsteroidMaterialConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material.WeightedMaterialConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material.WeightedRandomConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.AddConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.AsteroidSize
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.DomainWarpConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.EvaluationConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.FractalSettings
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.MaxConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.MultiplyConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.NoiseConfiguration2d
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.NoiseConfiguration3d
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.NoiseTypeConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.StaticConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.SubtractConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.SumConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.util.noise.PerlinOctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import kotlin.math.sqrt
import kotlin.random.Random

private val standardLayers =
	SumConfiguration(
		listOf(
			NoiseConfiguration3d(
				noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 100f,
				),
				fractalSettings = FractalSettings.FractalParameters(
					type = FractalSettings.NoiseFractalType.FBM,
					octaves = 3,
					lunacrity = 2f,
					gain = 1f,
					weightedStrength = 3f,
					pingPongStrength = 1f
				),
				domainWarpConfiguration = DomainWarpConfiguration.None,
				amplitude = 100.0
			),
			NoiseConfiguration3d(
				noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 25f,
				),
				fractalSettings = FractalSettings.None,
				domainWarpConfiguration = DomainWarpConfiguration.None,
				amplitude = 15.0
			),
			NoiseConfiguration3d(
				NoiseTypeConfiguration.Voronoi(
					featureSize = 10f,
					distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
					returnType = FastNoiseLite.CellularReturnType.Distance,
				),
				FractalSettings.None,
				DomainWarpConfiguration.None,
				10.0,
				normalizedPositive = false
			)
		)
	)

private val coronavirus =
	AddConfiguration(
		MultiplyConfiguration(
			AsteroidSize,
			StaticConfiguration(0.75)
		),
		SumConfiguration(
			listOf(
				NoiseConfiguration3d(
					noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
						featureSize = 150f,
					),
					fractalSettings = FractalSettings.FractalParameters(
						type = FractalSettings.NoiseFractalType.FBM,
						octaves = 3,
						lunacrity = 2f,
						gain = 1f,
						weightedStrength = 3f,
						pingPongStrength = 1f
					),
					domainWarpConfiguration = DomainWarpConfiguration.None,
					amplitude = 20.0,
					normalizedPositive = false
				),
				NoiseConfiguration3d(
					noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
						featureSize = 75f,
					),
					fractalSettings = FractalSettings.FractalParameters(
						type = FractalSettings.NoiseFractalType.FBM,
						octaves = 3,
						lunacrity = 2f,
						gain = 1f,
						weightedStrength = 3f,
						pingPongStrength = 1f
					),
					domainWarpConfiguration = DomainWarpConfiguration.None,
					amplitude = 20.0,
					normalizedPositive = false
				),
				NoiseConfiguration3d(
					NoiseTypeConfiguration.Voronoi(
						featureSize = 30f,
						distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
						returnType = FastNoiseLite.CellularReturnType.Distance,
					),
					FractalSettings.None,
					DomainWarpConfiguration.None,
					20.0,
					normalizedPositive = false
				)
			)
		)
	)

class ConfigurableAsteroidMeta(
	override val seed: Long,
	val size: Double,
	val block: Material,
	val oreBlobs: MutableList<OreBlob> = mutableListOf(),

	noiseLayers: EvaluationConfiguration = AddConfiguration(
		SumConfiguration(listOf(
			NoiseConfiguration3d(
				noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 150f,
				),
				fractalSettings = FractalSettings.FractalParameters(
					type = FractalSettings.NoiseFractalType.FBM,
					octaves = 3,
					lunacrity = 2f,
					gain = 1f,
					weightedStrength = 3f,
					pingPongStrength = 1f
				),
				domainWarpConfiguration = DomainWarpConfiguration.None,
				amplitude = 100.0
			),
			NoiseConfiguration3d(
				noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 25f,
				),
				fractalSettings = FractalSettings.None,
				domainWarpConfiguration = DomainWarpConfiguration.None,
				amplitude = 15.0
			),
			NoiseConfiguration3d(
				NoiseTypeConfiguration.Voronoi(
					featureSize = 10f,
					distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
					returnType = FastNoiseLite.CellularReturnType.Distance,
				),
				FractalSettings.None,
				DomainWarpConfiguration.None,
				10.0,
				normalizedPositive = false
			)
		)),
		MultiplyConfiguration(
			a = SubtractConfiguration(
				a = MaxConfiguration(
					a = NoiseConfiguration2d(
						NoiseTypeConfiguration.Voronoi(
							featureSize = 50f,
							distanceFunction = FastNoiseLite.CellularDistanceFunction.EuclideanSq,
							returnType = FastNoiseLite.CellularReturnType.Distance,
							1.0f
						),
						FractalSettings.None,
						DomainWarpConfiguration.None,
						1.0,
						normalizedPositive = true
					),
					b = StaticConfiguration(0.20),
				),
				b = StaticConfiguration(0.20)
			),
			b = StaticConfiguration(10.0)
		)
	),
	blockPlacerConfiguration: MaterialConfiguration = SurfaceDistanceAsteroidMaterialConfiguration(
		listOf(
			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.MAGMA_BLOCK), 10.0),
			WeightedMaterialConfiguration(NoiseMaterialConfiguration(
				noiseConfiguration = SubtractConfiguration(
					StaticConfiguration(1.0),
					NoiseConfiguration3d(
						NoiseTypeConfiguration.Voronoi(
							featureSize = 30f,
							distanceFunction = FastNoiseLite.CellularDistanceFunction.EuclideanSq,
							returnType = FastNoiseLite.CellularReturnType.CellValue,
							1.0f
						),
						FractalSettings.FractalParameters(
							type = FractalSettings.NoiseFractalType.FBM,
							octaves = 2,
							lunacrity = 2f,
							gain = 0.5f,
							weightedStrength = 0f,
							pingPongStrength = 0f
						),
						DomainWarpConfiguration.None,
						1.0,
						normalizedPositive = true
					)
				),
				blocks = listOf(
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STONE), 2.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLESTONE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.GRANITE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK), 1.0),
				)
			), 20.0),
			WeightedMaterialConfiguration(NoiseMaterialConfiguration(
				NoiseConfiguration3d(
					noiseTypeConfiguration = NoiseTypeConfiguration.Voronoi(
						featureSize = 100f,
						distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
						returnType = FastNoiseLite.CellularReturnType.Distance,
					),
					fractalSettings = FractalSettings.FractalParameters(
						type = FractalSettings.NoiseFractalType.FBM,
						octaves = 2,
						lunacrity = 2f,
						gain = 0.5f,
						weightedStrength = 0f,
						pingPongStrength = 0f
					),
					domainWarpConfiguration = DomainWarpConfiguration.None,
					amplitude = 1.0,
					normalizedPositive = true
				),
				listOf(
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_GLAZED_TERRACOTTA), 2.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUBE_CORAL_BLOCK), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_TERRACOTTA), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_CONCRETE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_ICE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ICE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_WOOL), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_GLAZED_TERRACOTTA), 1.0),
					WeightedMaterialConfiguration(WeightedRandomConfiguration(listOf(
						WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 50.0),
						WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SEA_LANTERN), 1.0),
					)), 2.0),
					WeightedMaterialConfiguration(NoiseMaterialConfiguration(
						NoiseConfiguration3d(
							NoiseTypeConfiguration.Voronoi(
								featureSize = 10f,
								distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
								returnType = FastNoiseLite.CellularReturnType.Distance,
							),
							FractalSettings.None,
							DomainWarpConfiguration.None,
							1.0,
							normalizedPositive = true
						),
						listOf(
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TERRACOTTA), 2.0),
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK), 2.0),
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 2.0),
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STONE), 2.0),
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLESTONE), 2.0),
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 2.0),
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLESTONE), 2.0),
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 2.0),
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK), 2.0),
							WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TERRACOTTA), 2.0),
						)
					), 12.5),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 2.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_GRAY_GLAZED_TERRACOTTA), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 1.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SNOW_BLOCK), 3.0),
					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 7.0),
				)
			), 5.0)
		),
		0.0,
		1.0
	)
//	blockPlacerConfiguration: MaterialConfiguration = SurfaceDistanceAsteroidMaterialConfiguration(
//		listOf(
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_GLAZED_TERRACOTTA), 3.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUBE_CORAL_BLOCK), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_TERRACOTTA), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_CONCRETE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_ICE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ICE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_WOOL), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_GLAZED_TERRACOTTA), 1.0),
//			WeightedMaterialConfiguration(WeightedRandomConfiguration(listOf(
//				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 50.0),
//				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SEA_LANTERN), 1.0),
//			)), 2.0),
//			WeightedMaterialConfiguration(NoiseMaterialConfiguration(
//				NoiseConfiguration3d(
//					NoiseConfiguration3d.NoiseTypeConfiguration.Voronoi(
//						featureSize = 10f,
//						distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
//						returnType = FastNoiseLite.CellularReturnType.Distance,
//					),
//					NoiseConfiguration3d.FractalSettings.None,
//					NoiseConfiguration3d.DomainWarpConfiguration.None,
//					1.0,
//					normalizedPositive = true
//				),
//				listOf(
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TERRACOTTA), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STONE), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLESTONE), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLESTONE), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TERRACOTTA), 2.0),
//				)
//			), 12.5),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 2.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_GRAY_GLAZED_TERRACOTTA), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SNOW_BLOCK), 3.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 7.0),
//		),
//		0.5,
//		0.95
//	)
//	blockPlacerConfiguration: MaterialConfiguration = NoiseMaterialConfiguration(
//		DivideConfiguration(
//			AddConfiguration(
//				NoiseConfiguration3d(
//					NoiseConfiguration3d.NoiseTypeConfiguration.Voronoi(
//						featureSize = 100f,
//						distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
//						returnType = FastNoiseLite.CellularReturnType.Distance,
//					),
//					NoiseConfiguration3d.FractalSettings.None,
//					NoiseConfiguration3d.DomainWarpConfiguration.None,
//					1.0,
//					normalizedPositive = true
//				),
//				NoiseConfiguration3d(
//					noiseTypeConfiguration = NoiseConfiguration3d.NoiseTypeConfiguration.OpenSimplex2(
//						featureSize = 30f,
//					),
//					fractalSettings = NoiseConfiguration3d.FractalSettings.FractalParameters(
//						type = NoiseConfiguration3d.FractalSettings.NoiseFractalType.FBM,
//						octaves = 3,
//						lunacrity = 2f,
//						gain = 1f,
//						weightedStrength = 3f,
//						pingPongStrength = 1f
//					),
//					domainWarpConfiguration = NoiseConfiguration3d.DomainWarpConfiguration.None,
//					amplitude = 0.25
//				)
//			),
//			StaticConfiguration(1.25),
//		),
//		listOf(
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_GLAZED_TERRACOTTA), 3.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUBE_CORAL_BLOCK), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_TERRACOTTA), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_CONCRETE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_ICE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ICE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_WOOL), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_GLAZED_TERRACOTTA), 1.0),
//			WeightedMaterialConfiguration(WeightedRandomConfiguration(listOf(
//				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 50.0),
//				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SEA_LANTERN), 1.0),
//			)), 2.0),
//			WeightedMaterialConfiguration(NoiseMaterialConfiguration(
//				NoiseConfiguration3d(
//					NoiseConfiguration3d.NoiseTypeConfiguration.Voronoi(
//						featureSize = 10f,
//						distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
//						returnType = FastNoiseLite.CellularReturnType.Distance,
//					),
//					NoiseConfiguration3d.FractalSettings.None,
//					NoiseConfiguration3d.DomainWarpConfiguration.None,
//					1.0,
//					normalizedPositive = true
//				),
//				listOf(
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TERRACOTTA), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STONE), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLESTONE), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLESTONE), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK), 2.0),
//					WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TERRACOTTA), 2.0),
//				)
//			), 12.5),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 2.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_GRAY_GLAZED_TERRACOTTA), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 1.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SNOW_BLOCK), 3.0),
//			WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 7.0),
//		)
//	)
) : FeatureMetaData {
	val random = Random(seed)
	val blockPlacer = blockPlacerConfiguration.build(this)
	private val noiseLayers = noiseLayers.build(this)

	override val factory: FeatureMetadataFactory<ConfigurableAsteroidMeta> = Factory

	val sizeSquared = size.squared()

	val cave1 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }
	val cave2 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }

	val materialNoise = SimplexOctaveGenerator(seed, 1).apply {
		this.setScale(0.15 / sqrt(size / 15))
	}

	val totalDisplacement = getMaxDisplacement()
	private val normalizingFactor = size / totalDisplacement

	fun getNoise(x: Double, y: Double, z: Double, start: FeatureStart): Double {
		return noiseLayers.getValue(x, y, z, Vec3i(start.x, start.y, start.z)) * normalizingFactor
	}

	private fun getMaxDisplacement(): Double {
		return noiseLayers.getFallbackValue()
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
