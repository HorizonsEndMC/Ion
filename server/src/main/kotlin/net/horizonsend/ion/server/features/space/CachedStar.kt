package net.horizonsend.ion.server.features.space

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.space.Star
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getSphereBlocks
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.block.data.BlockData
import org.bukkit.util.noise.SimplexNoiseGenerator

class CachedStar(
    val databaseId: Oid<Star>,
    override val name: String,
    spaceWorldName: String,
    location: Vec3i,
    size: Double,
	val seed: Long,
	val crustLayers: List<StarCrustLayer>
) : CelestialBody(spaceWorldName, location),
	NamedCelestialBody {
	companion object {
		private const val MAX_SIZE = 190
	}

	init {
		require(size > 0 && size <= 1)
	}

	val innerSphereRadius = (MAX_SIZE * size).toInt()
	val outerSphereRadius get() = innerSphereRadius + (crustLayers.maxOfOrNull { it.index } ?: 0)

	override fun createStructure(): Map<Vec3i, BlockState> {
		val crust = mutableMapOf<Vec3i, BlockState>()

		for ((index, crustNoise, crustMaterials) in crustLayers) {
			val random = SimplexNoiseGenerator(seed + crustMaterials.hashCode())

			val palette: List<BlockState> = crustMaterials.map(BlockData::nms)

			getSphereBlocks(innerSphereRadius + index).associateWithTo(crust) { (x, y, z) ->
				// number from -1 to 1
				val simplexNoise = random.noise(x.d() * crustNoise, y.d() * crustNoise, z.d() * crustNoise)

				val noise = (simplexNoise / 2.0 + 0.5)

				return@associateWithTo when {
					palette.isEmpty() -> Blocks.DIRT.defaultBlockState()
					else -> palette[(noise * palette.size).toInt()]
				}
			}
		}

		return crust
	}

	data class StarCrustLayer(val index: Int, val noise: Double, val materials: List<BlockData>)
}
