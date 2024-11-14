package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.FluidNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R3.block.impl.CraftLightningRod

class FluidTransportCache(holder: CacheHolder<FluidTransportCache>): TransportCache(holder) {
	override val type: CacheType = CacheType.FLUID
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.addDataHandler<CraftLightningRod>(Material.LIGHTNING_ROD) { data, _ -> FluidNode.LightningRodNode(data.facing.axis) }
		.addSimpleNode(Material.CRAFTING_TABLE, FluidNode.FluidExtractorNode)
		.addSimpleNode(Material.FLETCHING_TABLE, FluidNode.FluidInputNode)
		.addSimpleNode(Material.REDSTONE_BLOCK, FluidNode.FluidMergeNode)
		.addSimpleNode(Material.IRON_BLOCK, FluidNode.FluidMergeNode)
		.addSimpleNode(Material.LAPIS_BLOCK, FluidNode.FluidInvertedMergeNode)
		.build()

	override fun tickExtractor(location: BlockKey, delta: Double) {

	}
}
