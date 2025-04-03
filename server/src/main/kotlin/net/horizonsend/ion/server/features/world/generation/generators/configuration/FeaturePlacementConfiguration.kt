package net.horizonsend.ion.server.features.world.generation.generators.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.world.level.ChunkPos
import org.bukkit.World
import kotlin.random.Random

@Serializable
sealed interface FeaturePlacementConfiguration<T: FeatureMetaData> {
	val placementPriority: Int

	fun generatePlacements(world: World, chunk: ChunkPos, random: Random): List<Pair<Vec3i, T>>
}
