package net.horizonsend.ion.server.features.world.generation.generators.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.world.level.ChunkPos
import org.bukkit.Material
import kotlin.random.Random
import kotlin.random.asJavaRandom

@Serializable
class AsteroidPlacementConfiguration : FeaturePlacementConfiguration<ConfigurableAsteroidMeta> {
	override val placementPriority: Int = 0

	override fun generatePlacements(chunk: ChunkPos, random: Random): List<Pair<Vec3i, ConfigurableAsteroidMeta>> {
		val density = 0.0612
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

			val meta = generateMetaData(random, x, z)

			val y = random.asJavaRandom()
				.nextGaussian(84.0, 196 / 2.0).toInt()
				.coerceIn(1 + meta.totalDisplacement.toInt(), 383 - meta.totalDisplacement.toInt())

			list.add(Vec3i(x, y, z) to meta)
		}

		return list
	}

	fun generateMetaData(chunkRandom: Random, x: Int, z: Int): ConfigurableAsteroidMeta {
//		val material = Material.entries.filter { material -> material.isBlock }.random(chunkRandom)
		// chunkRandom.nextDouble(5.0, 40.0)
		return ConfigurableAsteroidMeta(chunkRandom.nextLong(), 150.0, Material.STONE)
	}
}
