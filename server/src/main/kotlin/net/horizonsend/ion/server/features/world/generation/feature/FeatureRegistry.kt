package net.horizonsend.ion.server.features.world.generation.feature

import org.bukkit.NamespacedKey

object FeatureRegistry {
	val features = mutableMapOf<NamespacedKey, GeneratedFeature<*>>()

	val ASTEROID = register(ConfigurableAsteroidFeature)

	fun <T: GeneratedFeature<*>> register(feature: T): T {
		features[feature.key] = feature
		return feature
	}

	operator fun get(key: NamespacedKey) = features[key]!!
}
