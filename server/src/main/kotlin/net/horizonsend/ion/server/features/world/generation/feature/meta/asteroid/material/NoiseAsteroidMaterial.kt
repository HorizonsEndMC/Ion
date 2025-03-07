package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material

import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.IterativeValueProvider
import net.horizonsend.ion.server.miscellaneous.utils.weightedEntry
import net.minecraft.world.level.block.state.BlockState

class NoiseAsteroidMaterial(
	val meta: ConfigurableAsteroidMeta,
	val noiseProvider: IterativeValueProvider,
	val blocks: List<AsteroidMaterial.WeightedMaterial>
) : AsteroidMaterial {
	private fun getBlock(
		noiseValue: Double,
		x: Double,
		y: Double,
		z: Double,
		distanceSquared: Double,
		meta: ConfigurableAsteroidMeta
	): BlockState {
		return blocks.weightedEntry(noiseValue) { it.weight }.material.getValue(x, y, z, distanceSquared, meta)
	}

	override fun getValue(
		x: Double,
		y: Double,
		z: Double,
		distanceSquared: Double,
		meta: ConfigurableAsteroidMeta,
	): BlockState {
		val noiseValue = noiseProvider.getValue(x, y, z, meta)
		return getBlock(noiseValue, x, y, z, distanceSquared, meta)
	}
}
