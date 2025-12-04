package net.horizonsend.ion.server.features.world.generation.feature

import net.horizonsend.ion.server.core.registration.keys.WorldGenerationFeatureKeys
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.wreck.WreckMetaData
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.world.level.ChunkPos
import org.bukkit.generator.ChunkGenerator

object WreckFeature : GeneratedFeature<WreckMetaData>(WorldGenerationFeatureKeys.WRECK) {
	override val placementPriority: Int = 1000
	override val metaFactory: FeatureMetadataFactory<WreckMetaData> = WreckMetaData.Factory

	override fun generateChunk(
		generator: IonWorldGenerator<*>,
		chunkPos: ChunkPos,
		chunkData: ChunkGenerator.ChunkData,
		start: FeatureStart,
		metaData: WreckMetaData,
		minY: Int,
		maxY: Int,
	) {
		val structure = metaData.structureId.getValue()

		for (x in 0..15) {
			val realX = chunkPos.x.shl(4) + x
			val xOffset = start.x - realX

			for (realY in minY..maxY) {
				val yOffset = start.y - realY

				for (z in 0..15) {
					val realZ = chunkPos.z.shl(4) + z
					val zOffset = start.z - realZ

					if (!structure.isInBounds(xOffset, yOffset, zOffset)) continue

					val blockData = structure.getBlockData(xOffset, yOffset, zOffset)
					if (blockData.material.isAir) continue

					chunkData.setBlock(x, realY, z, blockData)
				}
			}
		}
	}

	override fun getExtents(metaData: WreckMetaData): Pair<Vec3i, Vec3i> {
		val wreckStructure =  metaData.structureId.getValue()

		return Pair(wreckStructure.minPoint, wreckStructure.maxPoint)
	}
}

