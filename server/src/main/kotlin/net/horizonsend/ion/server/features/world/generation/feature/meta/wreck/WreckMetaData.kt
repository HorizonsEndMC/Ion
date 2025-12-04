package net.horizonsend.ion.server.features.world.generation.feature.meta.wreck

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.WreckStructureKeys
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.minecraft.nbt.CompoundTag

class WreckMetaData(override val seed: Long, val structureId: IonRegistryKey<WreckStructure, out WreckStructure>) : FeatureMetaData {
	override val factory: FeatureMetadataFactory<*> = Factory

	object Factory : FeatureMetadataFactory<WreckMetaData>() {
		override fun load(data: CompoundTag): WreckMetaData {
			return WreckMetaData(data.getLong("seed"), WreckStructureKeys.getOrTrow(data.getString("structure")))
		}

		override fun saveData(featureData: WreckMetaData): CompoundTag {
			val tag = CompoundTag()
			tag.putLong("seed", featureData.seed)
			tag.putString("structure", featureData.structureId.key)
			return tag
		}
	}
}
