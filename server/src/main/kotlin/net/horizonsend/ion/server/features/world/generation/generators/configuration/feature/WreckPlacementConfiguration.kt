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

@Serializable
class WreckPlacementConfiguration() : FeaturePlacementConfiguration<WreckMetaData> {
	// Placed after asteroids & other terrain features
	override val placementPriority: Int = 1000

	override fun getFeatureKey(): IonRegistryKey<GeneratedFeature<*>, GeneratedFeature<WreckMetaData>> = WorldGenerationFeatureKeys.WRECK


	override fun generatePlacements(world: World, chunk: ChunkPos, random: Random): List<Pair<Vec3i, WreckMetaData>> {
		val chunkStartX = chunk.x.shl(4)
		val chunkStartZ = chunk.z.shl(4)

		if (!random.nextBoolean()) return emptyList()

		return listOf(
			Vec3i(chunkStartX, 128, chunkStartZ) to WreckMetaData(random.nextLong(), WreckStructureKeys.TUTORIAL_ESCAPE_POD) //TODO
		)
	}
}
