package net.horizonsend.ion.server.features.world.generation.feature

import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.world.generation.feature.meta.AsteroidMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.features.world.generation.generators.configuration.AsteroidPlacementConfiguration
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.ChunkPos
import org.bukkit.Material
import kotlin.random.Random

object AsteroidFeature : GeneratedFeature<AsteroidMetaData>(NamespacedKeys.key("asteroid_normal"), AsteroidPlacementConfiguration()) {
	override val metaFactory: FeatureMetadataFactory<AsteroidMetaData> = AsteroidMetaData.Factory

	override suspend fun generateSection(
		generator: IonWorldGenerator<*>,
		chunkPos: ChunkPos,
		start: FeatureStart,
		metadata: AsteroidMetaData,
		sectionY: Int,
		sectionMin: Int,
		sectionMax: Int,
	): CompletedSection {
		val section = CompletedSection.empty(sectionY)
		val center = Vec3i(start.x, start.y, start.z)

		for (x in 0..15) for (y in 0..15) for (z in 0..15) {
			val realX = chunkPos.x.shl(4) + x
			val realZ = chunkPos.z.shl(4) + z
			val realY = sectionMin + y
			if (center.distance(realX, realY, realZ) > metadata.size) continue
			section.setBlock(x, y, z, BlockData(metadata.block.createBlockData().nms, null))
		}

		return section
	}

	override fun getExtents(metaData: AsteroidMetaData): Pair<Vec3i, Vec3i> {
		return Vec3i(-metaData.size.toInt(), -metaData.size.toInt(), -metaData.size.toInt()) to Vec3i(metaData.size.toInt(), metaData.size.toInt(), metaData.size.toInt())
	}

	override fun generateMetaData(chunkRandom: Random): AsteroidMetaData {
		val material = Material.entries.filter { material -> material.isBlock }.random(chunkRandom)

		return AsteroidMetaData(60.0, material)
	}
}
