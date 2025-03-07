package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import kotlinx.serialization.Serializable

@Serializable
sealed interface IterativeValueProvider {
	/** Gets a maximum value ignoring any state introduced by the coordinates. */
	fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double
	fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double
	fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double
}
