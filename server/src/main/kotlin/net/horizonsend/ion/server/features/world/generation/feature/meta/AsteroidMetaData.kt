package net.horizonsend.ion.server.features.world.generation.feature.meta

import net.minecraft.nbt.CompoundTag
import org.bukkit.Material

data class AsteroidMetaData(
	val size: Double,
	val block: Material
) : FeatureMetaData {
	override val factory: FeatureMetadataFactory<AsteroidMetaData> = Factory

	object Factory : FeatureMetadataFactory<AsteroidMetaData>() {
		override fun load(data: CompoundTag): AsteroidMetaData {
			return AsteroidMetaData(
				data.getDouble("size"),
				Material.valueOf(data.getString("block"))
			)
		}

		override fun saveData(featureData: AsteroidMetaData): CompoundTag {
			val tag = CompoundTag()
			tag.putDouble("size", featureData.size)
			tag.putString("block", featureData.block.name)
			return tag
		}
	}
}
