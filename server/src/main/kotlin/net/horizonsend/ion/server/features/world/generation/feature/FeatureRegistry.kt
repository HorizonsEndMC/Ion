package net.horizonsend.ion.server.features.world.generation.feature

import org.bukkit.NamespacedKey

object FeatureRegistry {
	val features = mutableMapOf<NamespacedKey, GeneratedFeature>()

	fun <T: GeneratedFeature> register(key: NamespacedKey, feature: T): T {
		features[key] = feature
		return feature
	}

	operator fun get(key: NamespacedKey) = features[key]!!
}
