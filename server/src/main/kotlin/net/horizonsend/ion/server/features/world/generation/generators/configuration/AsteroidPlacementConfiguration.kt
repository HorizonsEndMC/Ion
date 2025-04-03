package net.horizonsend.ion.server.features.world.generation.generators.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.FeatureRegistry
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.world.level.ChunkPos
import org.bukkit.Material
import org.bukkit.World
import kotlin.random.Random
import kotlin.random.asJavaRandom

@Serializable
data class AsteroidPlacementConfiguration(
	val density: Double = 0.0612,
) : FeaturePlacementConfiguration<ConfigurableAsteroidMeta> {
	override val placementPriority: Int = 0

	override fun getFeature(): GeneratedFeature<ConfigurableAsteroidMeta> = FeatureRegistry.ASTEROID

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

			val meta = generateMetaData(random, world, x, z)

			val y = random.nextInt(world.minHeight + meta.totalDisplacement.toInt(), world.maxHeight - meta.totalDisplacement.toInt())

			list.add(Vec3i(x, y, z) to meta)
		}

		return list
	}

	private fun generateMetaData(chunkRandom: Random, world: World, x: Int, z: Int): ConfigurableAsteroidMeta {
		val biome = world.getBiome(x, world.minHeight, z)

		return ConfigurableAsteroidMeta(
			chunkRandom.nextLong(),
			chunkRandom.nextDouble(75.0, 150.0),

			Material.STONE
		)
	}
}
