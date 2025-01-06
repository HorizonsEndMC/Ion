package net.horizonsend.ion.server.features.world.generation.feature

import net.horizonsend.ion.server.features.world.generation.generators.FeatureGenerator.FeatureGenerationData
import net.minecraft.nbt.CompoundTag
import org.bukkit.NamespacedKey

object FeatureFactoryRegistry {
	val features = mutableMapOf<NamespacedKey, GeneratedFeature>()

	fun <T: GeneratedFeature> register(key: NamespacedKey, feature: T): T {
		features[key] = feature
		return feature
	}

	operator fun get(key: NamespacedKey) = features[key]!!

	fun load(key: NamespacedKey, tag: CompoundTag): FeatureGenerationData {
		TODO()
	}
}
