package net.horizonsend.ion.server.features.world.generation.feature.meta

import net.minecraft.nbt.CompoundTag

object FallbackFeatureMetaData : FeatureMetaData {
	override val seed: Long = 0
	override val factory: FeatureMetadataFactory<*>
		get() = TODO("Not yet implemented")

	object Factory : FeatureMetadataFactory<FallbackFeatureMetaData>() {
		override fun load(data: CompoundTag): FallbackFeatureMetaData = FallbackFeatureMetaData
		override fun saveData(featureData: FallbackFeatureMetaData): CompoundTag = CompoundTag()
	}
}
