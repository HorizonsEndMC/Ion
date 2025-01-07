package net.horizonsend.ion.server.features.world.generation.generators.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.minecraft.world.level.ChunkPos
import kotlin.random.Random
import kotlin.random.asJavaRandom

@Serializable
class AsteroidPlacementConfiguration() : FeaturePlacementConfiguration {
	override val placementPriority: Int = 0

	override fun generatePlacements(chunk: ChunkPos, random: Random): List<Vec3i> {
		val density = 0.5
		val stdev = density / 2.0

		val count = random.asJavaRandom().nextGaussian(density, stdev).toInt().coerceAtLeast(0)

		val list = mutableListOf<Vec3i>()

		repeat(count) { i ->
			val chunkStartX = chunk.x.shl(4)
			val chunkStartZ = chunk.z.shl(4)

			list.add(Vec3i(
				chunkStartX + random.nextInt(0, 15),
				random.asJavaRandom().nextGaussian(192.0, 196 / 2.0).toInt().coerceIn(1, 383),
				chunkStartZ + random.nextInt(0, 15)
			))
		}

		return list
	}
}
