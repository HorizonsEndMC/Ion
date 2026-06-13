package net.horizonsend.ion.server.features.world.generation.feature.nms

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.keys.WorldGenerationFeatureKeys
import net.horizonsend.ion.server.features.world.generation.feature.FallbackFeature
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.meta.FallbackFeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.StructurePiece
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import org.bukkit.NamespacedKey

/**
 * Implementation of a NMS structure piece used to store arbitrary data for HE features
 **/
class PieceDataStorage(val pos: Vec3i, val feature: GeneratedFeature<*>, val metaData: FeatureMetaData) : StructurePiece(Type, 1, BoundingBox.infinite()) {
    override fun addAdditionalSaveData(context: StructurePieceSerializationContext, tag: CompoundTag) {
        runCatching {
			tag.putInt("x", pos.x)
			tag.putInt("y", pos.y)
			tag.putInt("z", pos.z)
			tag.putString("feature", feature.key.ionNamespacedKey.asString())

			tag.put("meta_data", feature.metaFactory.castAndSave(metaData))
		}
    }

    override fun postProcess(p0: WorldGenLevel, p1: StructureManager, p2: ChunkGenerator, p3: RandomSource, p4: BoundingBox, p5: ChunkPos, p6: BlockPos) {}

    companion object Type : StructurePieceType.StructureTemplateType {
        override fun load(context: StructureTemplateManager, tag: CompoundTag): StructurePiece = try {
            val namespacedKey = NamespacedKey.fromString(tag.getString("feature").get()) ?: throw IllegalArgumentException("Invalid namespaced key ${tag.getString("feature")}!")
            val feature = WorldGenerationFeatureKeys[namespacedKey]?.getValue() ?: throw NullPointerException("World generation feature ${namespacedKey.asString()} not found!")

            PieceDataStorage(
                Vec3i(tag.getInt("x").get(), tag.getInt("y").get(), tag.getInt("z").get()),
                feature,
                feature.metaFactory.load(tag.getCompound("meta_data").get())
            )
        } catch (e: Throwable) {
			IonServer.slF4JLogger.error("Error deserializing structure! ${e.message}")
			e.printStackTrace()

			// Load a blank fallback
			PieceDataStorage(
				Vec3i.ZERO,
				FallbackFeature,
				FallbackFeatureMetaData.Factory.load(tag)
			)
		}
    }
}
