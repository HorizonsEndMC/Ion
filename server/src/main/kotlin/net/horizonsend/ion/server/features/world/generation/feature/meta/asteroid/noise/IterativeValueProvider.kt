package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i

interface IterativeValueProvider {
	/** Gets a maximum value ignoring any state introduced by the coordinates. */
	fun getFallbackValue(): Double
	fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double
}
