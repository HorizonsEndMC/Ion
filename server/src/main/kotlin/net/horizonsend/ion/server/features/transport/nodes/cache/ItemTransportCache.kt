package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode.SolidGlassNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Material
import org.bukkit.craftbukkit.block.impl.CraftGrindstone

class ItemTransportCache(holder: CacheHolder<ItemTransportCache>): TransportCache(holder) {
	override val type: CacheType = CacheType.FLUID
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.addSimpleNode(STAINED_GLASS_TYPES) { _, material -> SolidGlassNode(ItemNode.PipeChannel[material]!!) }
		.addSimpleNode(STAINED_GLASS_TYPES) { _, material -> ItemNode.PaneGlassNode(ItemNode.PipeChannel[material]!!) }
		.addSimpleNode(Material.TINTED_GLASS, ItemNode.WildcardSolidGlassNode)
		.addSimpleNode(Material.GLASS, ItemNode.WildcardSolidGlassNode)
		.addSimpleNode(Material.GLASS_PANE, ItemNode.WildcardPaneGlassNode)
		.addDataHandler<CraftGrindstone>(Material.GRINDSTONE) { data, key -> ItemNode.ItemMergeNode }
		.build()

	override fun tickExtractor(location: BlockKey, delta: Double) {

	}
}
