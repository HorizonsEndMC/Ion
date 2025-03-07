package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material

import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.IterativeValueProvider
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.horizonsend.ion.server.miscellaneous.utils.weightedEntry
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material

interface AsteroidMaterial {
	fun getValue(
		x: Double,
		y: Double,
		z: Double,
		distanceSquared: Double,
		meta: ConfigurableAsteroidMeta,
	): BlockState

	data class WeightedMaterial(val material: AsteroidMaterial, val weight: Double) : AsteroidMaterial {
		override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, meta: ConfigurableAsteroidMeta): BlockState {
			return material.getValue(x, y, z, distanceSquared, meta)
		}
	}

	class SimpleMaterial(val state: BlockState) : AsteroidMaterial {
		constructor(material: Material) : this(material.createBlockData().nms)
		override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, meta: ConfigurableAsteroidMeta): BlockState = state
	}
}

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

class RamdomWeightedAsteroidMaterial(
	val blocks: List<AsteroidMaterial.WeightedMaterial>
) : AsteroidMaterial {
	override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, meta: ConfigurableAsteroidMeta): BlockState {
		return blocks.weightedRandom(meta.random) { it.weight }.getValue(x, y, z, distanceSquared, meta)
	}
}

class RamdomAsteroidMaterial(
	val blocks: List<AsteroidMaterial>
) : AsteroidMaterial {
	override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, meta: ConfigurableAsteroidMeta): BlockState {
		return blocks.random(meta.random).getValue(x, y, z, distanceSquared, meta)
	}
}
