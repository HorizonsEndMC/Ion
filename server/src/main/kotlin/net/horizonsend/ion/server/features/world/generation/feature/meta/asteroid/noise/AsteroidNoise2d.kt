package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import com.github.auburn.FastNoiseLite
import com.github.auburn.FastNoiseLite.Vector3
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.util.Vector
import kotlin.random.Random

class AsteroidNoise2d(
	private val xScale: Double = 1.0,
	private val yScale: Double = 1.0,
	private val zScale: Double = 1.0,
	val asteroidSize: Double,
	private val noise: FastNoiseLite,
	private val domainWarp: AsteroidNoise3d.DomainWarp,
	val amplitude: Double,
	private val normalizedPositive: Boolean
) : IterativeValueProvider {
	override fun getFallbackValue(): Double {
		return amplitude
	}

	fun setSeed(random: Random) {
		noise.SetSeed(random.nextInt())
		domainWarp.setSeed(random.nextInt())
	}

	override fun getValue(x: Double, y: Double, z: Double, origin: Vec3i): Double {
		val diff = Vector((x * xScale) - origin.x, (y * yScale) - origin.y, (z * zScale) - origin.z).normalize().multiply(asteroidSize)

		val vector = Vector3(
			diff.x.toFloat() + origin.x,
			diff.y.toFloat() + origin.y,
			diff.z.toFloat() + origin.z
		)

		domainWarp.warp(vector)
		var noiseValue = noise.GetNoise(vector.x, vector.y, vector.z)

		if (normalizedPositive) {
			noiseValue += 1
			noiseValue /= 2
		}

		return (noiseValue * amplitude)
	}
}
