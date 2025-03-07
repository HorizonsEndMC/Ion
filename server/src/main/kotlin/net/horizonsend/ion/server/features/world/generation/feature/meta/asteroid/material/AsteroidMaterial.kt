package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material

import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.miscellaneous.utils.nms
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
