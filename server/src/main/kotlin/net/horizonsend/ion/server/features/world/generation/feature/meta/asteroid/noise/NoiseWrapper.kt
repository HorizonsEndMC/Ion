package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import com.github.auburn.FastNoiseLite
import com.github.auburn.FastNoiseLite.Vector2
import com.github.auburn.FastNoiseLite.Vector3
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import kotlin.random.Random

class NoiseWrapper(
	private val noise: FastNoiseLite,
	private val domainWarp: DomainWarp,
	val amplitude: Double,
	private val normalizedPositive: Boolean
) : IterativeValueProvider {
	override fun getFallbackValue(meta: ConfigurableAsteroidMeta): Double {
		return amplitude
	}

	fun setSeed(random: Random) {
		noise.SetSeed(random.nextInt())
		domainWarp.setSeed(random.nextInt())
	}

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		val vector = Vector3(x.toFloat(), y.toFloat(), z.toFloat())
		domainWarp.warp(vector)
		var noiseValue = noise.GetNoise(vector.x, vector.y, vector.z)

		if (normalizedPositive) {
			noiseValue += 1
			noiseValue /= 2
		}

		return noiseValue * amplitude
	}
	override fun getValue(x: Double, z: Double, meta: ConfigurableAsteroidMeta): Double {
		val vector = Vector2(x.toFloat(), z.toFloat())
		domainWarp.warp(vector)

		var noiseValue =  noise.GetNoise(vector.x, vector.y)

		if (normalizedPositive) {
			noiseValue += 1
			noiseValue /= 2
		}

		return noiseValue * amplitude
	}

	sealed interface DomainWarp {
		fun warp(vector3: Vector3)
		fun warp(vector2: Vector2)

		fun setSeed(seed: Int)

		data object None : DomainWarp {
			override fun warp(vector3: Vector3) {}
			override fun warp(vector2: Vector2) {}
			override fun setSeed(seed: Int) {}
		}

		data class NoiseWarp(val noise: FastNoiseLite, val multplier: Float) : DomainWarp {
			override fun setSeed(seed: Int) {
				noise.SetSeed(seed)
			}

			override fun warp(vector3: Vector3) {
				val prevX = vector3.x
				val prevY = vector3.y
				val prevZ = vector3.z
				val cloned = Vector3(prevX, prevY, prevZ)

				noise.DomainWarp(vector3)

				val diffX = (vector3.x - prevX) * multplier
				val diffY = (vector3.y - prevY) * multplier
				val diffZ = (vector3.z - prevZ) * multplier

				vector3.x = cloned.x + diffX
				vector3.y = cloned.y + diffY
				vector3.z = cloned.z + diffZ
			}
			override fun warp(vector2: Vector2) {
				val prevX = vector2.x
				val prevY = vector2.y
				val cloned = Vector2(prevX, prevY)

				noise.DomainWarp(vector2)

				val diffX = (vector2.x - prevX) * multplier
				val diffY = (vector2.y - prevY) * multplier

				vector2.x = cloned.x + diffX
				vector2.y = cloned.y + diffY
			}
		}
	}
}
