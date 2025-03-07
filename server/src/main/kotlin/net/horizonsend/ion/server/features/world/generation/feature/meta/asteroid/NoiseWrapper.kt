package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import com.github.auburn.FastNoiseLite
import com.github.auburn.FastNoiseLite.Vector2
import com.github.auburn.FastNoiseLite.Vector3

class NoiseWrapper(
	private val noise: FastNoiseLite,
	private val domainWarp: DomainWarp,
	val blendMode: BlendMode,
	val amplitude: Float,
	private val normalizedPositive: Boolean
) {
	fun getNoise(x: Double, y: Double, z: Double): Float {
		val vector = Vector3(x.toFloat(), y.toFloat(), z.toFloat())
		domainWarp.warp(vector)

		var base =  noise.GetNoise(vector.x, vector.y, vector.z)

		if (normalizedPositive) {
			base += 1
			base /= 2
		}

		base *= amplitude

		return base
	}
	fun getNoise(x: Double, z: Double): Float {
		val vector = Vector2(x.toFloat(), z.toFloat())
		domainWarp.warp(vector)

		var base =  noise.GetNoise(vector.x, vector.y)

		if (normalizedPositive) {
			base += 1
			base /= 2
		}

		base *= amplitude

		return base
	}

	sealed interface DomainWarp {
		fun warp(vector3: Vector3)
		fun warp(vector2: Vector2)

		data object None : DomainWarp {
			override fun warp(vector3: Vector3) {}
			override fun warp(vector2: Vector2) {}
		}

		data class NoiseWarp(val noise: FastNoiseLite) : DomainWarp {
			override fun warp(vector3: Vector3) {
				noise.DomainWarp(vector3)
			}
			override fun warp(vector2: Vector2) {
				noise.DomainWarp(vector2)
			}
		}
	}
}
