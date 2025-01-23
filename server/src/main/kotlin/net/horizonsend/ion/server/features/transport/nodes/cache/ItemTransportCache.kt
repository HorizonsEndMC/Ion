package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.misc.TransportDebugCommand
import net.horizonsend.ion.server.command.misc.TransportDebugCommand.measureOrFallback
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.items.SortingOrder
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode.SolidGlassNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_PANE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import org.bukkit.Material
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.craftbukkit.block.impl.CraftGrindstone
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.Inventory

class ItemTransportCache(holder: CacheHolder<ItemTransportCache>): TransportCache(holder) {
	override val type: CacheType = CacheType.ITEMS
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.addSimpleNode(CRAFTING_TABLE, ItemNode.ItemExtractorNode)
		.addSimpleNode(CustomBlocks.ADVANCED_ITEM_EXTRACTOR, ItemNode.ItemExtractorNode)
		.addSimpleNode(STAINED_GLASS_TYPES) { _, material -> SolidGlassNode(ItemNode.PipeChannel[material]!!) }
		.addSimpleNode(STAINED_GLASS_PANE_TYPES) { _, material -> ItemNode.PaneGlassNode(ItemNode.PipeChannel[material]!!) }
		.addSimpleNode(Material.GLASS, SolidGlassNode(ItemNode.PipeChannel.CLEAR))
		.addSimpleNode(Material.GLASS_PANE, ItemNode.PaneGlassNode(ItemNode.PipeChannel.CLEAR))
		.addSimpleNode(Material.TINTED_GLASS, ItemNode.WildcardSolidGlassNode)
		.addDataHandler<CraftGrindstone>(Material.GRINDSTONE) { data, key -> ItemNode.ItemMergeNode }
		.addSimpleNode(
			Material.CHEST,
			Material.TRAPPED_CHEST,
			Material.BARREL,
			Material.FURNACE,
			Material.DISPENSER,
			Material.DROPPER,
			Material.DECORATED_POT
		) { key, _ -> ItemNode.InventoryNode(key) }
		.build()

	override fun tickExtractor(
		location: BlockKey,
		delta: Double,
		metaData: ExtractorMetaData?,
	) {
		return

		NewTransport.executor.submit {
			measureOrFallback(TransportDebugCommand.extractorTickTimes) {
				handleExtractorTick(location, delta, metaData as? ItemExtractorMetaData)
			}
		}
	}

	fun handleExtractorTick(location: BlockKey, delta: Double, meta: ItemExtractorMetaData?) {
		val distributionOrder = meta?.sortingOrder ?: SortingOrder.NEAREST_FIRST

		val sources = getSources(location)
		if (sources.isEmpty()) {
			println("No source inventories")
			return
		}

		val destinations: Collection<BlockKey> = getNetworkDestinations<ItemNode.InventoryNode>(location) { node ->
			getInventory(node.position) != null
		}

		debugAudience.information("Destinations: ${destinations.size}")
	}

	fun getInventory(key: BlockKey): Inventory? {
		val globalVec = holder.transportManager.getGlobalCoordinate(toVec3i(key))
		val nmsChunk = holder.getWorld().minecraft.getChunkIfLoaded(globalVec.x.shr(4), globalVec.z.shr(4)) ?: return null
		val tileEntity = nmsChunk.getBlockEntity(BlockPos(globalVec.x, globalVec.y, globalVec.z)) as? Container ?: return null

		return CraftInventory(tileEntity)
	}

	fun getSources(extractorLocation: BlockKey): Set<Inventory> {
		val inventories = mutableSetOf<Inventory>()

		for (face in ADJACENT_BLOCK_FACES) {
			val inventoryLocation = getRelative(extractorLocation, face)
			if (holder.getOrCacheGlobalNode(inventoryLocation) !is ItemNode.InventoryNode) continue
			val inv = getInventory(inventoryLocation) ?: continue
			if (inv.isEmpty) continue
			inventories.add(inv)
		}

		return inventories
	}
}
