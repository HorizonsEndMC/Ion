package net.horizonsend.ion.server.features.world.generation.feature.meta

import net.minecraft.nbt.CompoundTag

abstract class FeatureMetadataFactory<T: FeatureMetaData> {
	abstract fun load(data: CompoundTag): T

	@Suppress("UNCHECKED_CAST")
	fun castAndSave(data: FeatureMetaData): CompoundTag = saveData(data as T)
	abstract fun saveData(featureData: T): CompoundTag
}
