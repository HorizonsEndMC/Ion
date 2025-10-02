package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.WORLD_GENERATION_FEATURE
import net.horizonsend.ion.server.features.world.generation.feature.ConfigurableAsteroidFeature
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature

object WorldGenerationFeatureKeys : KeyRegistry<GeneratedFeature<*>>(WORLD_GENERATION_FEATURE, GeneratedFeature::class) {
	val CONFIGURABLE_ASTEROID = registerTypedKey<ConfigurableAsteroidFeature>("CONFIGURABLE_ASTEROID")
}
