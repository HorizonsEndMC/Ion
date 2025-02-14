package net.horizonsend.ion.server.features.world.generation.feature.start

import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.nms.IonStructureTypes
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.levelgen.structure.StructureStart
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer

data class FeatureStart(
	val feature: GeneratedFeature<*>,
	val x: Int,
	val y: Int,
	val z: Int,
	val metaData: FeatureMetaData,
	val seed: Long,
) {
	fun getNMS(): StructureStart {
		return StructureStart(
			feature.ionStructure.value(),
			ChunkPos(x.shr(4), z.shr(4)),
			0,
			PiecesContainer(listOf(IonStructureTypes.PieceDataStorage(Vec3i(x, y, z), seed, feature, metaData)))
		)
	}

	companion object {
		fun fromNMS(start: StructureStart): FeatureStart {
			val piece = start.pieces.first() as IonStructureTypes.PieceDataStorage
			return FeatureStart(
				piece.feature,
				piece.pos.x,
				piece.pos.y,
				piece.pos.z,
				piece.metaData,
				piece.seed
			)
		}
	}
}
