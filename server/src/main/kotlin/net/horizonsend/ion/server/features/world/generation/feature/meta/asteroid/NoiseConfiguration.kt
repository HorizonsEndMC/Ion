package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import com.github.auburn.FastNoiseLite
import com.github.auburn.FastNoiseLite.CellularDistanceFunction
import com.github.auburn.FastNoiseLite.CellularReturnType
import com.github.auburn.FastNoiseLite.DomainWarpType
import com.github.auburn.FastNoiseLite.FractalType
import com.github.auburn.FastNoiseLite.RotationType3D
import kotlinx.serialization.Serializable

@Serializable
data class NoiseConfiguration(
	val noiseTypeConfiguration: NoiseTypeConfiguration,
	val fractalSettings: FractalSettings,
	val domainWarpConfiguration: DomainWarpConfiguration,
	val amplitude: Double = 1.0,
	val normalizedPositive: Boolean = true
) {
	fun build(): NoiseWrapper {
		val instance = FastNoiseLite()
		noiseTypeConfiguration.apply(instance)
		fractalSettings.apply(instance)
		domainWarpConfiguration.apply(instance)

		return NoiseWrapper(instance, domainWarpConfiguration.build(), amplitude, normalizedPositive)
	}

	@Serializable
	sealed interface NoiseTypeConfiguration {
		fun apply(noiseLite: FastNoiseLite)

		@Serializable
		data class Perlin(
			val featureSize: Float,
		) : NoiseTypeConfiguration {
			override fun apply(noiseLite: FastNoiseLite) {
				noiseLite.SetNoiseType(FastNoiseLite.NoiseType.Perlin)
				noiseLite.SetFrequency(1 / featureSize)
			}
		}

		@Serializable
		data class OpenSimplex2(
			val featureSize: Float,
		) : NoiseTypeConfiguration {
			override fun apply(noiseLite: FastNoiseLite) {
				noiseLite.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2)
				noiseLite.SetFrequency(1 / featureSize)
			}
		}

		@Serializable
		data class OpenSimplex2S(
			val featureSize: Float,
		) : NoiseTypeConfiguration {
			override fun apply(noiseLite: FastNoiseLite) {
				noiseLite.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S)
				noiseLite.SetFrequency(1 / featureSize)
			}
		}

		@Serializable
		data class ValueCubic(
			val featureSize: Float,
		) : NoiseTypeConfiguration {
			override fun apply(noiseLite: FastNoiseLite) {
				noiseLite.SetNoiseType(FastNoiseLite.NoiseType.ValueCubic)
				noiseLite.SetFrequency(1 / featureSize)
			}
		}

		@Serializable
		data class Value(
			val featureSize: Float,
		) : NoiseTypeConfiguration {
			override fun apply(noiseLite: FastNoiseLite) {
				noiseLite.SetNoiseType(FastNoiseLite.NoiseType.Value)
				noiseLite.SetFrequency(1 / featureSize)
			}
		}

		@Serializable
		data class Voronoi(
			val featureSize: Float,
			val distanceFunction: CellularDistanceFunction,
			val returnType: CellularReturnType,
			val cellularJitter: Float = 1.0f,
		) : NoiseTypeConfiguration {
			override fun apply(noiseLite: FastNoiseLite) {
				noiseLite.SetFrequency(1 / featureSize)
				noiseLite.SetNoiseType(FastNoiseLite.NoiseType.Cellular)
				noiseLite.SetCellularDistanceFunction(distanceFunction)
				noiseLite.SetCellularReturnType(returnType)
				noiseLite.SetCellularJitter(cellularJitter)
			}
		}
	}

	@Serializable
	sealed interface FractalSettings {
		fun apply(noiseLite: FastNoiseLite)

		@Serializable
		data object None : FractalSettings {
			override fun apply(noiseLite: FastNoiseLite) {}
		}

		enum class NoiseFractalType(val internal: FractalType) {
			FBM(FractalType.FBm),
			RIDGED(FractalType.Ridged),
			PINGPONG(FractalType.PingPong)
		}

		@Serializable
		data class FractalParameters(
			val type: NoiseFractalType,
			val octaves: Int,
			val lunacrity: Float,
			val gain: Float,
			val weightedStrength: Float,
			val pingPongStrength: Float
		) : FractalSettings {
			override fun apply(noiseLite: FastNoiseLite) {
				noiseLite.SetFractalType(type.internal)
				noiseLite.SetFractalOctaves(octaves)
				noiseLite.SetFractalLacunarity(lunacrity)
				noiseLite.SetFractalGain(gain)
				noiseLite.SetFractalWeightedStrength(weightedStrength)
				noiseLite.SetFractalPingPongStrength(pingPongStrength)
			}
		}
	}

	@Serializable
	sealed interface DomainWarpConfiguration {
		fun apply(noiseLite: FastNoiseLite)
		fun build(): NoiseWrapper.DomainWarp

		@Serializable
		data object None : DomainWarpConfiguration {
			override fun apply(noiseLite: FastNoiseLite) {}
			override fun build(): NoiseWrapper.DomainWarp = NoiseWrapper.DomainWarp.None
		}

		@Serializable
		data class DomainWarpParameters(
			val domainWarpType: DomainWarpType,
			val rotationType3D: RotationType3D,
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

			override fun build(): NoiseWrapper.DomainWarp {
				val domainWarpNoise = FastNoiseLite()
				noiseType.apply(domainWarpNoise)
				fractalSettings.apply(domainWarpNoise)

				return NoiseWrapper.DomainWarp.NoiseWarp(domainWarpNoise, domainWarpMultiplier)
			}
		}
	}
}
