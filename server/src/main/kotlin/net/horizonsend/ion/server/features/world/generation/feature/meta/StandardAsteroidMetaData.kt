package net.horizonsend.ion.server.features.world.generation.feature.meta

import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material

class StandardAsteroidMetaData(
	val size: Double,
	val block: Material,
	val paletteBlocks: List<BlockState> = listOf(
		Material.QUARTZ_BRICKS.createBlockData().nms,
		Material.QUARTZ_BLOCK.createBlockData().nms,
		Material.DIORITE.createBlockData().nms,
		Material.POLISHED_DIORITE.createBlockData().nms,
		Material.RED_GLAZED_TERRACOTTA.createBlockData().nms,
		Material.NETHERRACK.createBlockData().nms,
		Material.RED_NETHER_BRICKS.createBlockData().nms,
	),
	val oreBlobs: MutableList<OreBlob> = mutableListOf(),
	val octaves: Int = 2,
) : FeatureMetaData {
	override val factory: FeatureMetadataFactory<StandardAsteroidMetaData> = Factory

	object Factory : FeatureMetadataFactory<StandardAsteroidMetaData>() {
		override fun load(data: CompoundTag): StandardAsteroidMetaData {
			return StandardAsteroidMetaData(
				data.getDouble("size"),
				Material.valueOf(data.getString("block"))
			)
		}

		override fun saveData(featureData: StandardAsteroidMetaData): CompoundTag {
			val tag = CompoundTag()
			tag.putDouble("size", featureData.size)
			tag.putString("block", featureData.block.name)
			return tag
		}
	}
}
