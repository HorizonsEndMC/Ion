package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import com.github.auburn.FastNoiseLite
import com.github.auburn.FastNoiseLite.Vector3
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.util.Vector
import kotlin.random.Random

class AsteroidNoise2d(
	val meta: ConfigurableAsteroidMeta,
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
		val diff = Vector(x - origin.x, y - origin.y, z - origin.z).normalize().multiply(meta.size)

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
