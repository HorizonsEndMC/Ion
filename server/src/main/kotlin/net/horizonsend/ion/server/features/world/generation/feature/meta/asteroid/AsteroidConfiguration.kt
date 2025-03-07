package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.NoiseConfiguration

@Serializable
data class AsteroidConfiguration(
	val noiseLayers: List<NoiseConfiguration>
) {
}
