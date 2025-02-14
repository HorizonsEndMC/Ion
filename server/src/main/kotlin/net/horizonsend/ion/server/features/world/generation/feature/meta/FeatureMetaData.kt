package net.horizonsend.ion.server.features.world.generation.feature.meta

import net.minecraft.nbt.CompoundTag

/**
 * Contains metadata about the feature. E.g. asteroid pallete or size, wreck variant
 **/
interface FeatureMetaData {
	val factory: FeatureMetadataFactory<*>

	fun save(): CompoundTag {
		return factory.castAndSave(this)
	}
}
