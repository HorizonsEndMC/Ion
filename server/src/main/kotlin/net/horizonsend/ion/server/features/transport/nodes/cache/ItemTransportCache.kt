package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.server.command.misc.TransportDebugCommand
import net.horizonsend.ion.server.command.misc.TransportDebugCommand.measureOrFallback
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.items.SortingOrder
import net.horizonsend.ion.server.features.transport.items.transaction.Change
import net.horizonsend.ion.server.features.transport.items.transaction.ItemTransaction
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode.SolidGlassNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.getBlockEntity
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_PANE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.minecraft.world.Container
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.Material
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.CommandBlock
import org.bukkit.block.data.type.Hopper
import org.bukkit.craftbukkit.block.impl.CraftGrindstone
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

class ItemTransportCache(override val holder: CacheHolder<ItemTransportCache>): TransportCache(holder) {
	override val type: CacheType = CacheType.ITEMS
	override val extractorNodeClass: KClass<out Node> = ItemNode.ItemExtractorNode::class
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.addSimpleNode(CRAFTING_TABLE, ItemNode.ItemExtractorNode)
		.addDataHandler<CommandBlock>(CustomBlocks.ADVANCED_ITEM_EXTRACTOR) { _, _ -> ItemNode.ItemExtractorNode }
		.addSimpleNode(STAINED_GLASS_TYPES) { _, material -> SolidGlassNode(ItemNode.PipeChannel[material]!!) }
		.addSimpleNode(STAINED_GLASS_PANE_TYPES) { _, material -> ItemNode.PaneGlassNode(ItemNode.PipeChannel[material]!!) }
		.addSimpleNode(Material.GLASS, SolidGlassNode(ItemNode.PipeChannel.CLEAR))
		.addSimpleNode(Material.GLASS_PANE, ItemNode.PaneGlassNode(ItemNode.PipeChannel.CLEAR))
		.addSimpleNode(Material.TINTED_GLASS, ItemNode.WildcardSolidGlassNode)
		.addDataHandler<CraftGrindstone>(Material.GRINDSTONE) { data, key -> ItemNode.ItemMergeNode }
		.addDataHandler<CommandBlock>(CustomBlocks.ITEM_FILTER) { data, key -> ItemNode.AdvancedFilterNode(key, this) }
		.addDataHandler<Hopper>(Material.HOPPER) { data, key -> ItemNode.HopperFilterNode(key, data.facing, this) }
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
		NewTransport.runTask {
			measureOrFallback(TransportDebugCommand.extractorTickTimes) {
				handleExtractorTick(location, delta, metaData as? ItemExtractorMetaData)
			}
		}
	}

	fun handleExtractorTick(location: BlockKey, delta: Double, meta: ItemExtractorMetaData?) {
		val distributionOrder = meta?.sortingOrder ?: SortingOrder.NEAREST_FIRST

		val sources = getSources(location)
		if (sources.isEmpty()) {
			return
		}

		val items = sources.flatMap { inv -> inv }.filterNotNull()
		if (items.isEmpty()) return

		val byCount = mutableMapOf<ItemStack, Int>()

		for (item in items) {
			val asOne = item.asOne()
			if (byCount.containsKey(asOne)) continue
			val count = items.sumOf { stack -> if (stack.isSimilar(item)) stack.amount else 0 }
			byCount[asOne] = count
		}

		debugAudience.information("counts: [${byCount.entries.joinToString { "${it.key.type}, ${it.value}]" }}, ${toVec3i(location)}")

		val originNode = getOrCache(location) ?: return

		for ((item, count) in byCount) {
			debugAudience.information("Checking ${item.type} [$count]")

			val destinations: List<BlockKey> = getNetworkDestinations<ItemNode.InventoryNode>(location, originNode) { node ->
				val inventory = getInventory(node.position)
				inventory != null && LegacyItemUtils.canFit(inventory, item, 1)
			}.toList()

			if (destinations.isEmpty()) {
				debugAudience.information("No destinations found")
				continue
			}

			val numDestinations = destinations.size

			val paths: Array<PathfindingReport?> = measureOrFallback(TransportDebugCommand.pathfindTimes) { Array(numDestinations) {
				findPath(
					origin = Node.NodePositionData(
						ItemNode.ItemExtractorNode,
						holder.getWorld(),
						location,
						BlockFace.SELF
					),
					destination = destinations[it],
					ignoreCache = true // TODO wait for a caching implementation that will allow compound keys for item types
				) { node, blockFace ->
					if (node !is ItemNode.FilterNode) return@findPath true
					debugAudience.serverError("checking filter")
					node.matches(item)
				}
			} }

//			var destinationMap = mutableMapOf<BlockKey, Int>()

			val validDestinations = destinations.filterIndexed { index, destination ->
				val path = paths[index]
//				path?.let { destinationMap[destination] = index }
				path != null
			}

			if (validDestinations.isEmpty()) {
				return
			}

			meta?.markItemPathfind(item)

			val destination = if (meta != null) {
				distributionOrder.getDestination(meta, validDestinations)
			} else {
				val extractorPosition = toVec3i(location)
				destinations.minBy { key -> extractorPosition.distance(toVec3i(key)) }
			}

			debugAudience.information("Selected destination ${toVec3i(destination)}")

			val transact = ItemTransaction(holder)

			for (source in sources) {
				val key = toBlockKey((source.inventory as BlockEntity).blockPos.toVec3i())
				transact.addRemoval(key, Change.ItemRemoval(item, count))
			}

			transact.addAddition(destination, Change.ItemAddition(item, count))

			transact.commit()

			debugAudience.highlightBlock(toVec3i(destination), 40L)
		}
	}

	fun getInventory(localKey: BlockKey): CraftInventory? {
		val tileEntity = getBlockEntity(holder.transportManager.getGlobalCoordinate(toVec3i(localKey)), holder.getWorld()) as? Container ?: return null
		return CraftInventory(tileEntity)
	}

	fun getSources(extractorLocation: BlockKey): Set<CraftInventory> {
		val inventories = mutableSetOf<CraftInventory>()

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
