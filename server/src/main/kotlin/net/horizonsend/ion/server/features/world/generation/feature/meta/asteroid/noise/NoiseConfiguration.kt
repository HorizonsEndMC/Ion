package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import com.github.auburn.FastNoiseLite
import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta

@Serializable
sealed interface NoiseConfiguration : EvaluationConfiguration {
	val noiseTypeConfiguration: NoiseTypeConfiguration
	val fractalSettings: FractalSettings
	val domainWarpConfiguration: DomainWarpConfiguration
	val amplitude: Double
	val normalizedPositive: Boolean
}

@Serializable
data class NoiseConfiguration3d(
	override val noiseTypeConfiguration: NoiseTypeConfiguration,
	override val fractalSettings: FractalSettings,
	override val domainWarpConfiguration: DomainWarpConfiguration,
	override val amplitude: Double = 1.0,
	override val normalizedPositive: Boolean = true
) : NoiseConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): AsteroidNoise3d {
		val instance = FastNoiseLite()
		noiseTypeConfiguration.apply(instance)
		fractalSettings.apply(instance)
		domainWarpConfiguration.apply(instance)

		val wrapper = AsteroidNoise3d(meta, instance, domainWarpConfiguration.build(), amplitude, normalizedPositive)
		wrapper.setSeed(meta.random)
		return wrapper
	}
}

@Serializable
data class NoiseConfiguration2d(
	override val noiseTypeConfiguration: NoiseTypeConfiguration,
	override val fractalSettings: FractalSettings,
	override val domainWarpConfiguration: DomainWarpConfiguration,
	override val amplitude: Double = 1.0,
	override val normalizedPositive: Boolean = true
) : NoiseConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): AsteroidNoise2d {
		val instance = FastNoiseLite()
		noiseTypeConfiguration.apply(instance)
		fractalSettings.apply(instance)
		domainWarpConfiguration.apply(instance)

		val wrapper = AsteroidNoise2d(meta, instance, domainWarpConfiguration.build(), amplitude, normalizedPositive)
		wrapper.setSeed(meta.random)
		return wrapper
	}
}
