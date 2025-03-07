package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import kotlinx.serialization.Serializable

@Serializable
data class AsteroidConfiguration(
	val startValue: StartValue,
	val noiseLayers: List<NoiseConfiguration>
) {
	enum class StartValue {
		ASTEROID_SIZE {
			override fun getValue(meta: ConfigurableAsteroidMeta): Double {
				return meta.size
			}
		},
		NONE {
			override fun getValue(meta: ConfigurableAsteroidMeta): Double {
				return 0.0
			}
		};

		abstract fun getValue(meta: ConfigurableAsteroidMeta): Double
	}
}
