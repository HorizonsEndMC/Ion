package net.horizonsend.ion.server.features.world.generation.feature.start

import net.horizonsend.ion.server.features.world.generation.feature.FeatureRegistry
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.minecraft.nbt.CompoundTag
import org.bukkit.NamespacedKey

data class FeatureStart(
	val feature: GeneratedFeature<*>,
	val x: Int,
	val y: Int,
	val z: Int,
	val metaData: FeatureMetaData
) {
	fun save(): CompoundTag {
		val base = CompoundTag()

		base.putString(FEATURE_KEY, feature.key.toString())

		base.putInt(X_KEY, x)
		base.putInt(Y_KEY, y)
		base.putInt(Z_KEY, z)

		base.put(META_KEY, metaData.save())

		return base
	}

	companion object {
		private const val FEATURE_KEY = "feature"
		private const val META_KEY = "meta"
		private const val X_KEY = "x"
		private const val Y_KEY = "y"
		private const val Z_KEY = "z"

		fun load(data: CompoundTag): FeatureStart {
			val x = data.getInt(X_KEY)
			val y = data.getInt(Y_KEY)
			val z = data.getInt(Z_KEY)
			val featureKey = NamespacedKey.fromString(data.getString(FEATURE_KEY)) ?: throw IllegalArgumentException("Invalid namespace key")
			val feature = FeatureRegistry[featureKey]

			val metaDataCompound = data.getCompound(META_KEY)
			val metaData = feature.metaFactory.load(metaDataCompound)

			return FeatureStart(feature, x, y ,z, metaData)
		}
	}
}
