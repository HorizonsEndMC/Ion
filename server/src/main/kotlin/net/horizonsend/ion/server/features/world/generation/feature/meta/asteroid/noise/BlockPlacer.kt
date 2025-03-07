package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.miscellaneous.utils.weightedEntry
import net.minecraft.world.level.block.state.BlockState

class BlockPlacer(
	val blocks: List<PlacedBlockState>
) {
	private fun getBlock(noiseValue: Double): BlockState {
		return blocks.weightedEntry(noiseValue) { it.weight }.state
	}

	fun getValue(
		x: Double,
		y: Double,
		z: Double,
		distanceSquared: Double,
		meta: ConfigurableAsteroidMeta,
	): BlockState {
		val noiseGenerator = NoiseConfiguration(
			noiseTypeConfiguration = NoiseConfiguration.NoiseTypeConfiguration.Perlin(featureSize = 10f),
			fractalSettings = NoiseConfiguration.FractalSettings.None,
			domainWarpConfiguration = NoiseConfiguration.DomainWarpConfiguration.None,
			amplitude = 1.0,
			normalizedPositive = true
		).build()

		val noiseValue = noiseGenerator.getValue(x, y, z, meta)
		return getBlock(noiseValue)
	}

	data class PlacedBlockState(val state: BlockState, val weight: Double)
}
