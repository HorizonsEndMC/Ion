package net.horizonsend.ion.server.features.world.generation.generators.configuration.feature

import com.github.auburn.FastNoiseLite
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.WorldGenerationFeatureRegistry
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material.MaterialConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.EvaluationConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.FractalSettings
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.NoiseTypeConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.weightedEntry
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import net.minecraft.world.level.ChunkPos
import org.bukkit.World
import kotlin.random.Random
import kotlin.random.asJavaRandom

@Serializable
data class AsteroidPlacementConfiguration(
	val density: Double = 0.0612,

	val selector: AsteroidSelectorCondition = AsteroidSelectorCondition.IfBiome(
		biomeKeys = listOf("minecraft:small_end_islands"),
		ifTrue = AsteroidSelectorCondition.BuilderReference("TEST"),
		ifFalse = AsteroidSelectorCondition.WeightedNoise(
			noiseTypeConfiguration = NoiseTypeConfiguration.OpenSimplex2(featureSize = 2500f),
			fractalSettings = FractalSettings.None,
			weightedBuilders = listOf(
//				AsteroidSelectorCondition.BuilderReference("TEST") to 1.0,
//				AsteroidSelectorCondition.BuilderReference("TEST2") to 1.0,
//				AsteroidSelectorCondition.BuilderReference("TEST3") to 1.0,
				AsteroidSelectorCondition.BuilderReference("TEST4") to 1.0,
				AsteroidSelectorCondition.BuilderReference("TEST9") to 1.0,
				AsteroidSelectorCondition.BuilderReference("TEST7") to 1.0,
				AsteroidSelectorCondition.BuilderReference("TEST5") to 1.0,
				AsteroidSelectorCondition.BuilderReference("TEST6") to 1.0,
				AsteroidSelectorCondition.BuilderReference("TEST8") to 1.0,
			),
		),
	)
) : FeaturePlacementConfiguration<ConfigurableAsteroidMeta> {
	override val placementPriority: Int = 0

	override fun getFeature(): GeneratedFeature<ConfigurableAsteroidMeta> = WorldGenerationFeatureRegistry.ASTEROID

	override fun generatePlacements(world: World, chunk: ChunkPos, random: Random): List<Pair<Vec3i, ConfigurableAsteroidMeta>> {
		val stdev = density * 4.0

		val count = random.asJavaRandom()
			.nextGaussian(density, stdev)
			.plus(stdev)
			.coerceAtLeast(0.0)
			.toInt()

		val list = mutableListOf<Pair<Vec3i, ConfigurableAsteroidMeta>>()

		repeat(count) {
			val chunkStartX = chunk.x.shl(4)
			val chunkStartZ = chunk.z.shl(4)

			val x = chunkStartX + random.nextInt(0, 15)
			val z = chunkStartZ + random.nextInt(0, 15)

			val meta = generateMetaData(random, world, x, z) ?: return@repeat

			val y = random.nextInt(world.minHeight + meta.totalDisplacement.toInt(), world.maxHeight - meta.totalDisplacement.toInt())

			list.add(Vec3i(x, y, z) to meta)
		}

		return list
	}

	private fun generateMetaData(chunkRandom: Random, world: World, x: Int, z: Int): ConfigurableAsteroidMeta? {
		val builder = ConfigurationFiles.globalAsteroidConfiguration().builders[selector.getBuilder(chunkRandom, world, x, z)]

		return builder?.build(
			ConfigurationFiles.globalAsteroidConfiguration().structureTemplates,
			ConfigurationFiles.globalAsteroidConfiguration().paletteTemplates,
			chunkRandom.nextLong(),
			chunkRandom.nextDouble(75.0, 150.0),
		)
	}

	@Serializable
	sealed interface AsteroidBuilder {
		fun build(structureMap: Map<String, EvaluationConfiguration>, paletteMap: Map<String, MaterialConfiguration>, seed: Long, size: Double): ConfigurableAsteroidMeta

		@Serializable
		data class RandomCombination(val structures: Map<String, Double>, val palettes: Map<String, Double>) : AsteroidBuilder {
			override fun build(structureMap: Map<String, EvaluationConfiguration>, paletteMap: Map<String, MaterialConfiguration>, seed: Long, size: Double): ConfigurableAsteroidMeta {
				val aliasedNoiseLayersKey = structures.entries.weightedRandom { it.value }.key
				val aliasedNoiseLayers = structureMap[aliasedNoiseLayersKey]!!
				val aliasedBlockPlacerConfigurationKey = palettes.entries.weightedRandom { it.value }.key
				val aliasedBlockPlacerConfiguration = paletteMap[aliasedBlockPlacerConfigurationKey]!!

				return ConfigurableAsteroidMeta(
					seed,
					size,
					oreBlobs = mutableListOf(),
					aliasedNoiseLayers = aliasedNoiseLayersKey to aliasedNoiseLayers,
					aliasedBlockPlacerConfiguration = aliasedBlockPlacerConfigurationKey to aliasedBlockPlacerConfiguration
				)
			}
		}

		@Serializable
		data class StaticCombination(val structure: String, val palette: String) : AsteroidBuilder {
			override fun build(structureMap: Map<String, EvaluationConfiguration>, paletteMap: Map<String, MaterialConfiguration>, seed: Long, size: Double): ConfigurableAsteroidMeta {
				return ConfigurableAsteroidMeta(
					seed,
					size,
					oreBlobs = mutableListOf(),
					aliasedNoiseLayers = structure to structureMap[structure]!!,
					aliasedBlockPlacerConfiguration = palette to paletteMap[palette]!!
				)
			}
		}
	}

	@Serializable
	sealed interface AsteroidSelectorCondition {
		fun getBuilder(chunkRandom: Random, world: World, x: Int, z: Int): String

		@Serializable
		data class BuilderReference(val key: String): AsteroidSelectorCondition {
			override fun getBuilder(chunkRandom: Random, world: World, x: Int, z: Int): String = key
		}

		@Serializable
		data object Nothing: AsteroidSelectorCondition {
			override fun getBuilder(chunkRandom: Random, world: World, x: Int, z: Int): String = "NULL_ASTEROID_KEY"
		}

		@Serializable
		data class IfBiome(
			val biomeKeys: List<String>,
			val ifTrue: AsteroidSelectorCondition,
			val ifFalse: AsteroidSelectorCondition,
		): AsteroidSelectorCondition {
			override fun getBuilder(chunkRandom: Random, world: World, x: Int, z: Int): String {
				val biome = world.getBiome(x, world.minHeight, z)
				return if (biomeKeys.contains(biome.key.asString())) ifTrue.getBuilder(chunkRandom, world, x, z) else ifFalse.getBuilder(chunkRandom, world, x, z)
			}
		}

		@Serializable
		data class WeightedRandom(
			val weightedBuilders: List<Pair<AsteroidSelectorCondition, Double>>
		): AsteroidSelectorCondition {
			override fun getBuilder(chunkRandom: Random, world: World, x: Int, z: Int): String {
				return weightedBuilders.weightedRandom { it.second }.first.getBuilder(chunkRandom, world, x, z)
			}
		}

		@Serializable
		data class WeightedNoise(
			val noiseTypeConfiguration: NoiseTypeConfiguration,
			val fractalSettings: FractalSettings,

			val weightedBuilders: List<Pair<AsteroidSelectorCondition, Double>>
		): AsteroidSelectorCondition {
			@Transient
			private val noise: FastNoiseLite = FastNoiseLite().apply {
				noiseTypeConfiguration.apply(this)
				fractalSettings.apply(this)
			}

			private fun getNoise(x: Int, z: Int): Double {
				var noiseValue = noise.GetNoise(x.toFloat(), z.toFloat())
				noiseValue += 1
				noiseValue /= 2
				return noiseValue.toDouble()
			}

			override fun getBuilder(chunkRandom: Random, world: World, x: Int, z: Int): String {
				return weightedBuilders.weightedEntry(getNoise(x, z)) { it.second }.first.getBuilder(chunkRandom, world, x, z)
			}
		}
	}
}
