package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import com.github.auburn.FastNoiseLite
import kotlinx.serialization.Serializable

@Serializable
sealed interface DomainWarpConfiguration {
	fun apply(noiseLite: FastNoiseLite)
	fun build(): AsteroidNoise3d.DomainWarp

	@Serializable
	data object None : DomainWarpConfiguration {
		override fun apply(noiseLite: FastNoiseLite) {}
		override fun build(): AsteroidNoise3d.DomainWarp = AsteroidNoise3d.DomainWarp.None
	}

	@Serializable
	data class DomainWarpParameters(
		val domainWarpType: FastNoiseLite.DomainWarpType,
		val rotationType3D: FastNoiseLite.RotationType3D,
		val amplitude: Float,
		val noiseType: NoiseTypeConfiguration,
		val fractalSettings: FractalSettings,
		val domainWarpMultiplier: Float
	) : DomainWarpConfiguration {
		override fun apply(noiseLite: FastNoiseLite) {
			noiseLite.SetDomainWarpType(domainWarpType)
			noiseLite.SetRotationType3D(rotationType3D)
			noiseLite.SetDomainWarpAmp(amplitude)
		}

		override fun build(): AsteroidNoise3d.DomainWarp {
			val domainWarpNoise = FastNoiseLite()
			noiseType.apply(domainWarpNoise)
			fractalSettings.apply(domainWarpNoise)

			return AsteroidNoise3d.DomainWarp.NoiseWarp(domainWarpNoise, domainWarpMultiplier)
		}
	}
}
