package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.miscellaneous.utils.nms
import org.bukkit.Material

@Serializable
data class BlockPlacementConfiguration(
	val blocks: List<PlacedBlockConfiguration>
) {
	fun build(): BlockPlacer = BlockPlacer(blocks.map { it.build() })

	@Serializable
	data class PlacedBlockConfiguration(
		val material: Material,
		val weight: Double
	) {
		fun build(): BlockPlacer.PlacedBlockState = BlockPlacer.PlacedBlockState(material.createBlockData().nms, weight)
	}
}
