package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import com.github.auburn.FastNoiseLite
import kotlinx.serialization.Serializable

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
		val distanceFunction: FastNoiseLite.CellularDistanceFunction,
		val returnType: FastNoiseLite.CellularReturnType,
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
