package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import com.github.auburn.FastNoiseLite
import kotlinx.serialization.Serializable

@Serializable
sealed interface FractalSettings {
	fun apply(noiseLite: FastNoiseLite)

	@Serializable
	data object None : FractalSettings {
		override fun apply(noiseLite: FastNoiseLite) {}
	}

	enum class NoiseFractalType(val internal: FastNoiseLite.FractalType) {
		FBM(FastNoiseLite.FractalType.FBm),
		RIDGED(FastNoiseLite.FractalType.Ridged),
		PINGPONG(FastNoiseLite.FractalType.PingPong)
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
