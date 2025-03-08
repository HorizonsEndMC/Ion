package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material

import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.IterativeValueProvider
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.horizonsend.ion.server.miscellaneous.utils.weightedEntry
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import kotlin.math.sqrt

interface AsteroidMaterial {
	fun getValue(
		x: Double,
		y: Double,
		z: Double,
		distanceSquared: Double,
		noise: Double,
		meta: ConfigurableAsteroidMeta,
	): BlockState

	data class WeightedMaterial(val material: AsteroidMaterial, val weight: Double) : AsteroidMaterial {
		override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, noise: Double, meta: ConfigurableAsteroidMeta): BlockState {
			return material.getValue(x, y, z, distanceSquared, noise, meta)
		}
	}

	class SimpleMaterial(val state: BlockState) : AsteroidMaterial {
		constructor(material: Material) : this(material.createBlockData().nms)
		override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, noise: Double, meta: ConfigurableAsteroidMeta): BlockState = state
	}
}

class NoiseAsteroidMaterial(
	val meta: ConfigurableAsteroidMeta,
	val noiseProvider: IterativeValueProvider,
	val blocks: List<AsteroidMaterial.WeightedMaterial>
) : AsteroidMaterial {
	override fun getValue(
		x: Double,
		y: Double,
		z: Double,
		distanceSquared: Double,
		noise: Double,
		meta: ConfigurableAsteroidMeta,
	): BlockState {
		val noiseValue = noiseProvider.getValue(x, y, z, meta)
		return blocks.weightedEntry(noiseValue) { it.weight }.material.getValue(x, y, z, distanceSquared, noise, meta)
	}
}

class RamdomWeightedAsteroidMaterial(
	val blocks: List<AsteroidMaterial.WeightedMaterial>
) : AsteroidMaterial {
	override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, noise: Double, meta: ConfigurableAsteroidMeta): BlockState {
		return blocks.weightedRandom(meta.random) { it.weight }.getValue(x, y, z, distanceSquared, noise, meta)
	}
}

class RamdomAsteroidMaterial(
	val blocks: List<AsteroidMaterial>
) : AsteroidMaterial {
	override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, noise: Double, meta: ConfigurableAsteroidMeta): BlockState {
		return blocks.random(meta.random).getValue(x, y, z, distanceSquared, noise, meta)
	}
}

class RelativeSurfaceDistanceAsteroidMaterial(
	val blocks: List<AsteroidMaterial.WeightedMaterial>,
	private val minRatio: Double,
	private val maxRatio: Double
) : AsteroidMaterial {
	override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, noise: Double, meta: ConfigurableAsteroidMeta): BlockState {
		val sqrtDistance = sqrt(distanceSquared)
		val distanceRatio = sqrtDistance / noise
		// Concentrate into the outer ratio
		if (distanceRatio < minRatio) return blocks.first().getValue(x, y, z, distanceSquared, noise, meta)
		if (distanceRatio > maxRatio) return blocks.last().getValue(x, y, z, distanceSquared, noise, meta)

		val position = (distanceRatio - minRatio) / (maxRatio - minRatio)

		return blocks.weightedEntry(position) { it.weight }.getValue(x, y, z, distanceSquared, noise, meta)
	}
}

class AbsoluteSurfaceDistanceAsteroidMaterial(
	val blocks: List<AsteroidMaterial.WeightedMaterial>,
	private val ratioStart: Double,
	private val ratioEnd: Double
) : AsteroidMaterial {
	override fun getValue(x: Double, y: Double, z: Double, distanceSquared: Double, noise: Double, meta: ConfigurableAsteroidMeta): BlockState {
		val sqrtDistance = sqrt(distanceSquared)
		// Concentrate into the outer ratio
		if (sqrtDistance < ratioStart) return blocks.first().getValue(x, y, z, distanceSquared, noise, meta)
		if (sqrtDistance > ratioEnd) return blocks.last().getValue(x, y, z, distanceSquared, noise, meta)

		val position = (sqrtDistance - ratioStart) / (ratioEnd - ratioStart)

		return blocks.weightedEntry(position) { it.weight }.getValue(x, y, z, distanceSquared, noise, meta)
	}
}

