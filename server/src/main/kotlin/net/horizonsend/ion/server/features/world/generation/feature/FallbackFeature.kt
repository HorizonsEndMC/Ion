package net.horizonsend.ion.server.features.world.generation.feature

import net.horizonsend.ion.server.core.registration.keys.WorldGenerationFeatureKeys
import net.horizonsend.ion.server.features.world.generation.feature.meta.FallbackFeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.world.level.ChunkPos
import org.bukkit.generator.ChunkGenerator

object FallbackFeature : GeneratedFeature<FallbackFeatureMetaData>(WorldGenerationFeatureKeys.FALLBACK) {
	override val placementPriority: Int = 0
	override val metaFactory: FeatureMetadataFactory<FallbackFeatureMetaData> = FallbackFeatureMetaData.Factory

	override fun generateChunk(
		generator: IonWorldGenerator<*>,
		chunkPos: ChunkPos,
		chunkData: ChunkGenerator.ChunkData,
		start: FeatureStart,
		metaData: FallbackFeatureMetaData,
		minY: Int,
		maxY: Int,
	) {}

	override fun getExtents(metaData: FallbackFeatureMetaData): Pair<Vec3i, Vec3i> = Pair(Vec3i.ZERO, Vec3i.ZERO)
}
