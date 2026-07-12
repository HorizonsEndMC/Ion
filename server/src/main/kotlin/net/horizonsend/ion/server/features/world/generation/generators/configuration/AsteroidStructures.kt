package net.horizonsend.ion.server.features.world.generation.generators.configuration

import com.github.auburn.FastNoiseLite
import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.world.generation.feature.meta.OreDefinition
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
object AsteroidStructures {
    val defaultStructureTemplates: Map<String, EvaluationConfiguration> = mapOf(
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
    )

    val defaultPaletteTemplates: Map<String, MaterialConfiguration> = mapOf(
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
                    ), 20.0
                ),
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
                            WeightedMaterialConfiguration(
                                SimpleMaterialConfiguration(Material.BLUE_GLAZED_TERRACOTTA),
                                2.0
                            ),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUBE_CORAL_BLOCK), 1.0),
                            WeightedMaterialConfiguration(
                                SimpleMaterialConfiguration(Material.LIGHT_BLUE_TERRACOTTA),
                                1.0
                            ),
                            WeightedMaterialConfiguration(
                                SimpleMaterialConfiguration(Material.LIGHT_BLUE_CONCRETE),
                                1.0
                            ),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_ICE), 1.0),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 1.0),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ICE), 1.0),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_WOOL), 1.0),
                            WeightedMaterialConfiguration(
                                SimpleMaterialConfiguration(Material.LIGHT_BLUE_GLAZED_TERRACOTTA),
                                1.0
                            ),
                            WeightedMaterialConfiguration(
                                WeightedRandomConfiguration(
                                    listOf(
                                        WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 50.0),
                                        WeightedMaterialConfiguration(
                                            SimpleMaterialConfiguration(Material.SEA_LANTERN),
                                            1.0
                                        ),
                                    )
                                ), 2.0
                            ),
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
                                        WeightedMaterialConfiguration(
                                            SimpleMaterialConfiguration(Material.TERRACOTTA),
                                            2.0
                                        ),
                                        WeightedMaterialConfiguration(
                                            SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK),
                                            2.0
                                        ),
                                        WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 2.0),
                                        WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STONE), 2.0),
                                        WeightedMaterialConfiguration(
                                            SimpleMaterialConfiguration(Material.COBBLESTONE),
                                            2.0
                                        ),
                                        WeightedMaterialConfiguration(
                                            SimpleMaterialConfiguration(Material.ANDESITE),
                                            2.0
                                        ),
                                        WeightedMaterialConfiguration(
                                            SimpleMaterialConfiguration(Material.COBBLESTONE),
                                            2.0
                                        ),
                                        WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 2.0),
                                        WeightedMaterialConfiguration(
                                            SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK),
                                            2.0
                                        ),
                                        WeightedMaterialConfiguration(
                                            SimpleMaterialConfiguration(Material.TERRACOTTA),
                                            2.0
                                        ),
                                    )
                                ), 12.5
                            ),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 2.0),
                            WeightedMaterialConfiguration(
                                SimpleMaterialConfiguration(Material.LIGHT_GRAY_GLAZED_TERRACOTTA),
                                1.0
                            ),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 1.0),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 1.0),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SNOW_BLOCK), 3.0),
                            WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 7.0),
                        )
                    ), 5.0
                )
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
        "CLASSIC_ANCIENT_DEBRIS_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),

            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DEAD_TUBE_CORAL_BLOCK), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BASALT), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_BLACKSTONE), 8.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.NETHER_BRICKS), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.NETHERRACK), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.RED_GLAZED_TERRACOTTA), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.NETHERRACK), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.RED_NETHER_BRICKS), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.NETHER_BRICKS), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_BLACKSTONE), 8.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BASALT), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DEAD_TUBE_CORAL_BLOCK), 6.0),
            )
        ),
        "CLASSIC_NETHER_QUARTZ_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.MAGMA_BLOCK), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.GRANITE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.NETHERRACK), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_GRANITE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.RED_TERRACOTTA), 2.0),
            )
        ),
        "CLASSIC_COAL_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLACKSTONE), 5.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_BLACKSTONE), 8.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DEEPSLATE), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.RED_SANDSTONE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_RED_SANDSTONE), 4.0),
            )
        ),
        "CLASSIC_COPPER_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIRT), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.GRANITE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_ICE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_MUD), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_GRANITE), 3.0),
            )
        ),
        "CLASSIC_IRON_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.GRANITE), 8.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_GRANITE), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_MUD), 7.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DRIPSTONE_BLOCK), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DEEPSLATE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_BASALT), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STONE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_ANDESITE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLESTONE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 6.0),
            )
        ),
        "CLASSIC_EMERALD_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.VERDANT_FROGLIGHT), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIME_TERRACOTTA), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DARK_PRISMARINE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PURPLE_TERRACOTTA), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.MAGENTA_TERRACOTTA), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PURPLE_TERRACOTTA), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLED_DEEPSLATE), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DEEPSLATE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_DIORITE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 4.0),
            )
        ),
        "CLASSIC_URANIUM_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.VERDANT_FROGLIGHT), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SLIME_BLOCK), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIME_TERRACOTTA), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.END_STONE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.END_STONE_BRICKS), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SANDSTONE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_SANDSTONE), 4.0),
            )
        ),
        "CLASSIC_GOLD_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SANDSTONE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_SANDSTONE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SANDSTONE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.OCHRE_FROGLIGHT), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SANDSTONE), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_DIORITE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_DIORITE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CLAY), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 3.0),
            )
        ),
        "CLASSIC_REDSTONE_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.RED_NETHER_BRICKS), 8.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.NETHERRACK), 5.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.RED_GLAZED_TERRACOTTA), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_DIORITE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.QUARTZ_BRICKS), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.QUARTZ_BLOCK), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_QUARTZ), 3.0),
            )
        ),
        "CLASSIC_DIAMOND_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_ICE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ICE), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PRISMARINE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PRISMARINE_BRICKS), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_DIORITE), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.QUARTZ_BLOCK), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_QUARTZ), 6.0),
            )
        ),
        "CLASSIC_ALUMINUM_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 8.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SNOW_BLOCK), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CALCITE), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DIORITE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 5.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 12.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_BASALT), 3.0),
            )
        ),
        "CLASSIC_TITANIUM_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SEA_LANTERN), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 5.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CYAN_TERRACOTTA), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STONE), 8.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CYAN_TERRACOTTA), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 2.0),
            )
        ),
        "CLASSIC_LAPIS_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CRYING_OBSIDIAN), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLACKSTONE), 2.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_BLACKSTONE), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 6.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CYAN_TERRACOTTA), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.MUD), 4.0),
            )
        ),
        "CLASSIC_CHETHERITE_PALETTE" to NoiseMaterialConfiguration(
            NoiseConfiguration3d(
                noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
                    featureSize = 20f
                ),
                fractalSettings = FractalSettings.None,
                domainWarpConfiguration = DomainWarpConfiguration.None,
                amplitude = 1.0,
                normalizedPositive = true
            ),
            listOf(
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PEARLESCENT_FROGLIGHT), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PURPUR_BLOCK), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.AMETHYST_BLOCK), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BUDDING_AMETHYST), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.AMETHYST_BLOCK), 3.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLUE_TERRACOTTA), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 4.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.ANDESITE), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.TUFF), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DEEPSLATE), 1.0),
                WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLED_DEEPSLATE), 1.0),
            )
        ),
		"SCORDITE_PALETTE" to NoiseMaterialConfiguration(
			NoiseConfiguration3d(
				noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 20f
				),
				fractalSettings = FractalSettings.None,
				domainWarpConfiguration = DomainWarpConfiguration.None,
				amplitude = 1.0,
				normalizedPositive = true
			),
			listOf(
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.MOSS_BLOCK), 3.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.GREEN_CONCRETE), 2.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.GREEN_TERRACOTTA), 1.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.MOSSY_STONE_BRICKS), 6.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLED_DEEPSLATE), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STONE_BRICKS), 2.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_TUFF), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CHISELED_TUFF), 4.0),
			)
		),
		"VANADIUM_PALETTE" to NoiseMaterialConfiguration(
			NoiseConfiguration3d(
				noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 20f
				),
				fractalSettings = FractalSettings.None,
				domainWarpConfiguration = DomainWarpConfiguration.None,
				amplitude = 1.0,
				normalizedPositive = true
			),
			listOf(
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_CONCRETE), 3.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PACKED_ICE), 2.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.LIGHT_BLUE_TERRACOTTA), 1.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.GRAY_GLAZED_TERRACOTTA), 6.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BASALT), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.DEEPSLATE), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 2.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.WARPED_HYPHAE), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.MUD), 4.0),
			)
		),
		"ZIRCON_PALETTE" to NoiseMaterialConfiguration(
			NoiseConfiguration3d(
				noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 20f
				),
				fractalSettings = FractalSettings.None,
				domainWarpConfiguration = DomainWarpConfiguration.None,
				amplitude = 1.0,
				normalizedPositive = true
			),
			listOf(
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CHISELED_TUFF), 3.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_TUFF), 2.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STONE_BRICKS), 1.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 6.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.GRAY_CONCRETE), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.POLISHED_BLACKSTONE), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CHERRY_WOOD), 2.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.CHISELED_NETHER_BRICKS), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.PURPLE_TERRACOTTA), 4.0),
			)
		),
		"ATAVUM_PALETTE" to NoiseMaterialConfiguration(
			NoiseConfiguration3d(
				noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(
					featureSize = 20f
				),
				fractalSettings = FractalSettings.None,
				domainWarpConfiguration = DomainWarpConfiguration.None,
				amplitude = 1.0,
				normalizedPositive = true
			),
			listOf(
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.BLACKSTONE), 3.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.MUD), 2.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.COBBLED_DEEPSLATE), 1.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.SMOOTH_BASALT), 6.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.GRAY_CONCRETE), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.RED_NETHER_BRICKS), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.STRIPPED_MANGROVE_WOOD), 2.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.RED_TERRACOTTA), 4.0),
				WeightedMaterialConfiguration(SimpleMaterialConfiguration(Material.RED_GLAZED_TERRACOTTA), 4.0),
			)
		),
    )


    val defaultBuilders: Map<String, AsteroidBuilder> = mapOf(
        "TEST" to AsteroidBuilder.StaticCombination("CLASSIC", "POLKA", mutableListOf(OreDefinition(OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_BLOCK), 0, 0.005))),
        "TEST2" to AsteroidBuilder.StaticCombination("CORONAVIRUS", "POLKA", mutableListOf(OreDefinition(OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_BLOCK), 0, 0.005))),
        "TEST3" to AsteroidBuilder.StaticCombination("CLASSIC", "POLKA", mutableListOf(OreDefinition(OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_BLOCK), 0, 0.005))),
        "TEST4" to AsteroidBuilder.StaticCombination("CLASSIC", "RED", mutableListOf(OreDefinition(OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_BLOCK), 0, 0.005))),
        "TEST5" to AsteroidBuilder.StaticCombination("CLASSIC", "GREEN", mutableListOf(OreDefinition(OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_BLOCK), 0, 0.005))),
        "TEST6" to AsteroidBuilder.StaticCombination("CLASSIC", "BLUE", mutableListOf(OreDefinition(OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_BLOCK), 0, 0.005))),
        "TEST7" to AsteroidBuilder.StaticCombination("CLASSIC", "YELLOW", mutableListOf(OreDefinition(OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_BLOCK), 0, 0.005))),
        "TEST8" to AsteroidBuilder.StaticCombination("CLASSIC", "PURPLE", mutableListOf(OreDefinition(OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_BLOCK), 0, 0.005))),
        "TEST9" to AsteroidBuilder.StaticCombination("CLASSIC", "ORANGE", mutableListOf(OreDefinition(OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_BLOCK), 0, 0.005))),

        "TRENCH_ANCIENT_DEBRIS" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_ANCIENT_DEBRIS_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.ANCIENT_DEBRIS),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_NETHER_QUARTZ" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_NETHER_QUARTZ_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.NETHER_QUARTZ_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_COAL" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_COAL_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.COAL_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_COPPER" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_COPPER_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.COPPER_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_IRON" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_IRON_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.IRON_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_EMERALD" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_EMERALD_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.DEEPSLATE_EMERALD_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_URANIUM" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_URANIUM_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.URANIUM_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_GOLD" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_GOLD_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.GOLD_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_REDSTONE" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_REDSTONE_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.REDSTONE_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_DIAMOND" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_DIAMOND_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.DIAMOND_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_ALUMINUM" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_ALUMINUM_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.ALUMINUM_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_TITANIUM" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_TITANIUM_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.TITANIUM_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_LAPIS" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_LAPIS_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.MaterialType(Material.DEEPSLATE_LAPIS_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),
        "TRENCH_CHETHERITE" to AsteroidBuilder.StaticCombination(
            "CLASSIC",
            "CLASSIC_CHETHERITE_PALETTE",
            mutableListOf(
                OreDefinition(
                    OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_ORE),
                    0,
                    0.019 / 8
                )
            )
        ),

		// MINING_WORLD
		"MINING_WORLD_ANCIENT_DEBRIS" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_ANCIENT_DEBRIS_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.ANCIENT_DEBRIS),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_NETHER_QUARTZ" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_NETHER_QUARTZ_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.NETHER_QUARTZ_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_COAL" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_COAL_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.COAL_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_COPPER" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_COPPER_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.COPPER_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_IRON" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_IRON_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.IRON_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_EMERALD" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_EMERALD_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.DEEPSLATE_EMERALD_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_URANIUM" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_URANIUM_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.URANIUM_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_GOLD" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_GOLD_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.GOLD_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_REDSTONE" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_REDSTONE_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.REDSTONE_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_DIAMOND" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_DIAMOND_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.DIAMOND_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_ALUMINUM" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_ALUMINUM_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.ALUMINUM_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_TITANIUM" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_TITANIUM_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.TITANIUM_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_LAPIS" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_LAPIS_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.DEEPSLATE_LAPIS_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_CHETHERITE" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_CHETHERITE_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_ORE),
					0,
					0.025 / 8
				)
			)
		),
		"MINING_WORLD_SCORDITE" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"SCORDITE_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.SCORDITE_ORE),
					0,
					0.004 / 8
				)
			)
		),
		"MINING_WORLD_VANADIUM" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"VANADIUM_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.VANADIUM_ORE),
					0,
					0.004 / 8
				)
			)
		),
		"TEST_WORLD_ZIRCON" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"ZIRCON_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.ZIRCON_ORE),
					0,
					0.003 / 8
				)
			)
		),
		"TEST_WORLD_ATAVUM" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"ATAVUM_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.ATAVUM_ORE),
					0,
					0.001 / 8
				)
			)
		),

		// DEEP_SPACE
		"DEEP_SPACE_IRON" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_IRON_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.MaterialType(Material.IRON_ORE),
					0,
					0.012 / 8
				)
			)
		),
		"DEEP_SPACE_CHETHERITE" to AsteroidBuilder.StaticCombination(
			"CLASSIC",
			"CLASSIC_CHETHERITE_PALETTE",
			mutableListOf(
				OreDefinition(
					OreDefinition.BlockType.CustomBlockType(CustomBlockKeys.CHETHERITE_ORE),
					0,
					0.012 / 8
				)
			)
		),
    )
}
