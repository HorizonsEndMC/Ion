package net.horizonsend.ion.server.features.world.generation.generators.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.minecraft.world.level.ChunkPos
import kotlin.random.Random

@Serializable
sealed interface FeaturePlacementConfiguration {
	val placementPriority: Int

	fun generatePlacements(chunk: ChunkPos, random: Random): List<Vec3i>
}
