package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.WORLD_GENERATION_FEATURE
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta

object WorldGenerationFeatureKeys : KeyRegistry<GeneratedFeature<*>>(WORLD_GENERATION_FEATURE, GeneratedFeature::class) {
	val CONFIGURABLE_ASTEROID = registerTypedKey<GeneratedFeature<ConfigurableAsteroidMeta>>("CONFIGURABLE_ASTEROID")
}
