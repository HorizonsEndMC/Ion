package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import com.github.auburn.FastNoiseLite
import com.github.auburn.FastNoiseLite.Vector3
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import org.bukkit.util.Vector
import kotlin.random.Random

class NoiseWrapper2d(
	private val noise: FastNoiseLite,
	private val domainWarp: NoiseWrapper3d.DomainWarp,
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

	override fun getValue(x: Double, y: Double, z: Double, meta: ConfigurableAsteroidMeta, start: FeatureStart): Double {
		val diff = Vector(
			x - start.x,
			y - start.y,
			z - start.z
		).normalize().multiply(meta.size)

		val vector = Vector3(
			diff.x.toFloat() + start.x,
			diff.y.toFloat() + start.y,
			diff.z.toFloat() + start.z
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
