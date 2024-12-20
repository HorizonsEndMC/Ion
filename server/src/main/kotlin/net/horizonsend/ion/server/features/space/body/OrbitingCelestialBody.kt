package net.horizonsend.ion.server.features.space.body

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import kotlin.math.cos
import kotlin.math.sin

interface OrbitingCelestialBody {
	val orbitDistance: Int
	val orbitSpeed: Double
	val orbitProgress: Double

	fun getParentLocation(): Vec3i

	companion object {
		fun calculateOrbitLocation(parentLocation: Vec3i, orbitDistance: Int, orbitProgress: Double): Vec3i {
			val (x, y, z) = parentLocation

			val radians = Math.toRadians(orbitProgress)

			return Vec3i(
				x = x + (cos(radians) * orbitDistance.d()).i(),
				y = y,
				z = z + (sin(radians) * orbitDistance.d()).i()
			)
		}
	}

	fun setOrbitProgress(progress: Double)

	fun changeOrbitDistance(newDistance: Int)

	fun orbit(updateDb: Boolean = true)
}
