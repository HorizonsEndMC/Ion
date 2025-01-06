package net.horizonsend.ion.server.features.world.generation.generators.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.FeaturePlacementContext
import net.horizonsend.ion.server.features.world.generation.generators.FeatureGenerator
import org.bukkit.Chunk

@Serializable
sealed interface FeaturePlacementConfiguration {
	val placementPriority: Int
	fun getCount(): Int
	fun canPlace(): Boolean

	fun generatePlacementContext(generator: FeatureGenerator, chunk: Chunk): FeaturePlacementContext
}
