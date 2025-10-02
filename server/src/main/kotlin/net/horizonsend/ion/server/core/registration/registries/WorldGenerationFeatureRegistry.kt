package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.WORLD_GENERATION_FEATURE
import net.horizonsend.ion.server.core.registration.keys.WorldGenerationFeatureKeys
import net.horizonsend.ion.server.features.world.generation.feature.ConfigurableAsteroidFeature
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature

class WorldGenerationFeatureRegistry : Registry<GeneratedFeature<*>>(WORLD_GENERATION_FEATURE) {
	override fun getKeySet(): KeyRegistry<GeneratedFeature<*>> = WorldGenerationFeatureKeys

	override fun boostrap() {
		register(WorldGenerationFeatureKeys.CONFIGURABLE_ASTEROID, ConfigurableAsteroidFeature)
	}
}
