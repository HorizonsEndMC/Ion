package net.horizonsend.ion.server.features.world.generation.generators.configuration.feature

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.WorldGenerationFeatureKeys
import net.horizonsend.ion.server.core.registration.keys.WreckStructureKeys
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.meta.wreck.WreckMetaData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.world.level.ChunkPos
import org.bukkit.World
import kotlin.random.Random
import kotlin.random.asJavaRandom

@Serializable
class WreckPlacementConfiguration(val density: Double) : FeaturePlacementConfiguration<WreckMetaData> {
	// Placed after asteroids & other terrain features
	override val placementPriority: Int = 1000

	override fun getFeatureKey(): IonRegistryKey<GeneratedFeature<*>, GeneratedFeature<WreckMetaData>> = WorldGenerationFeatureKeys.WRECK

	override fun generatePlacements(world: World, chunk: ChunkPos, random: Random): List<Pair<Vec3i, WreckMetaData>> {
		if (!random.nextBoolean()) return emptyList()

		val stDev = density * 4.0

		val count = random.asJavaRandom()
			.nextGaussian(density, stDev)
			.plus(stDev)
			.coerceAtLeast(0.0)
			.toInt()

		val list = mutableListOf<Pair<Vec3i, WreckMetaData>>()

		val chunkStartX = chunk.x.shl(4)
		val chunkStartZ = chunk.z.shl(4)

		repeat(count) {
			val x = chunkStartX + random.nextInt(0, 15)
			val z = chunkStartZ + random.nextInt(0, 15)

			val meta = generateMetaData(random, world, x, z)

			val y = random.nextInt(
				world.minHeight + meta.structureId.getValue().minPoint.y,
				world.maxHeight - meta.structureId.getValue().maxPoint.y
			)

			list.add(Vec3i(x, y, z) to meta)
		}

		return list
	}

	private fun generateMetaData(random: Random, world: World, x: Int, z: Int): WreckMetaData {
		return WreckMetaData(random.nextLong(), WreckStructureKeys.TUTORIAL_ESCAPE_POD)
	}
}
