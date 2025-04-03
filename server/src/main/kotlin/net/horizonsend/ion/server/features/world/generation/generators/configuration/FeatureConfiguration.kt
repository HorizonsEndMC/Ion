package net.horizonsend.ion.server.features.world.generation.generators.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.GeneratedFeature

@Serializable
sealed interface FeatureConfiguration {
	fun loadFeature(): GeneratedFeature<*>
}
