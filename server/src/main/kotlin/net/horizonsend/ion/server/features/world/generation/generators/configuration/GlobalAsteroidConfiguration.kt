package net.horizonsend.ion.server.features.world.generation.generators.configuration

import com.github.auburn.FastNoiseLite
import kotlinx.serialization.Serializable
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
import net.horizonsend.ion.server.features.world.generation.generators.configuration.feature.AsteroidPlacementConfiguration.AsteroidBuilder
import org.bukkit.Material

@Serializable
class GlobalAsteroidConfiguration(
	val structureTemplates: Map<String, EvaluationConfiguration> = mapOf(
		"CLASSIC" to SumConfiguration(
			listOf(
				NoiseConfiguration3d(
					noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
						featureSize = 100f,
					),
					fractalSettings = FractalSettings.FractalParameters(
						fractalType = FractalSettings.NoiseFractalType.FBM,
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
		),
		"CORONAVIRUS" to AddConfiguration(
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
							fractalType = FractalSettings.NoiseFractalType.FBM,
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
							fractalType = FractalSettings.NoiseFractalType.FBM,
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
		),
		"PLATES" to AddConfiguration(
			SumConfiguration(listOf(
				NoiseConfiguration3d(
					yScale = 3.5,
					noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
						featureSize = 150f,
					),
					fractalSettings = FractalSettings.FractalParameters(
						fractalType = FractalSettings.NoiseFractalType.FBM,
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
					yScale = 3.5,
					noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
						featureSize = 25f,
					),
					fractalSettings = FractalSettings.None,
					domainWarpConfiguration = DomainWarpConfiguration.None,
					amplitude = 15.0
				),
				NoiseConfiguration3d(
					noiseTypeConfiguration = NoiseTypeConfiguration.Voronoi(
						featureSize = 10f,
						distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
						returnType = FastNoiseLite.CellularReturnType.Distance,
					),
					fractalSettings = FractalSettings.None,
					domainWarpConfiguration = DomainWarpConfiguration.None,
					amplitude = 10.0,
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
	),

	val paletteTemplates: Map<String, MaterialConfiguration> = mapOf(
		"POLKA" to SurfaceDistanceAsteroidMaterialConfiguration(
			listOf(
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.MAGMA_BLOCK), 10.0),
				WeightedMaterialConfiguration(
					NoiseMaterialConfiguration(
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
									fractalType = FractalSettings.NoiseFractalType.FBM,
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
				WeightedMaterialConfiguration(
					NoiseMaterialConfiguration(
						NoiseConfiguration3d(
							noiseTypeConfiguration = NoiseTypeConfiguration.Voronoi(
								featureSize = 100f,
								distanceFunction = FastNoiseLite.CellularDistanceFunction.Euclidean,
								returnType = FastNoiseLite.CellularReturnType.Distance,
							),
							fractalSettings = FractalSettings.FractalParameters(
								fractalType = FractalSettings.NoiseFractalType.FBM,
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
							WeightedMaterialConfiguration(
								WeightedRandomConfiguration(listOf(
									WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 50.0),
									WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SEA_LANTERN), 1.0),
								)), 2.0),
							WeightedMaterialConfiguration(
								NoiseMaterialConfiguration(
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
		),
		"RED" to SimpleMaterialConfiguration(Material.RED_CONCRETE),
		"GREEN" to SimpleMaterialConfiguration(Material.GREEN_CONCRETE),
		"BLUE" to SimpleMaterialConfiguration(Material.BLUE_CONCRETE),
		"YELLOW" to SimpleMaterialConfiguration(Material.YELLOW_CONCRETE),
		"PURPLE" to SimpleMaterialConfiguration(Material.PURPLE_CONCRETE),
		"ORANGE" to SimpleMaterialConfiguration(Material.ORANGE_CONCRETE),
	),

	val builders: Map<String, AsteroidBuilder> = mapOf(
		"TEST" to AsteroidBuilder.StaticCombination("PLATES", "POLKA"),
		"TEST2" to AsteroidBuilder.StaticCombination("CORONAVIRUS", "POLKA"),
		"TEST3" to AsteroidBuilder.StaticCombination("CLASSIC", "POLKA"),
		"TEST4" to AsteroidBuilder.StaticCombination("CLASSIC", "RED"),
		"TEST5" to AsteroidBuilder.StaticCombination("CLASSIC", "GREEN"),
		"TEST6" to AsteroidBuilder.StaticCombination("CLASSIC", "BLUE"),
		"TEST7" to AsteroidBuilder.StaticCombination("CLASSIC", "YELLOW"),
		"TEST8" to AsteroidBuilder.StaticCombination("CLASSIC", "PURPLE"),
		"TEST9" to AsteroidBuilder.StaticCombination("CLASSIC", "ORANGE"),
	)
)
