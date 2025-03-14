package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.NoiseConfiguration3d

@Serializable
data class AsteroidSpecification(
	val noiseLayers: List<NoiseConfiguration3d>
) {
}
