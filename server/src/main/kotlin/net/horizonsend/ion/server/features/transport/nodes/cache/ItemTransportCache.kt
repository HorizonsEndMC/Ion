package net.horizonsend.ion.server.features.transport.nodes.cache

import com.google.common.collect.Multimap
import net.horizonsend.ion.server.command.misc.TransportDebugCommand
import net.horizonsend.ion.server.command.misc.TransportDebugCommand.measureOrFallback
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.items.transaction.ItemReference
import net.horizonsend.ion.server.features.transport.items.transaction.ItemTransaction
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.getBlockEntity
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.minecraft.world.Container
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

class ItemTransportCache(override val holder: CacheHolder<ItemTransportCache>): TransportCache(holder) {
	override val type: CacheType = CacheType.ITEMS
	override val extractorNodeClass: KClass<out Node> = ItemNode.ItemExtractorNode::class

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
		val sources = getSources(location)
		if (sources.isEmpty()) {
			return
		}

		val byInventory: Map<CraftInventory, List<IndexedValue<ItemStack>>> = sources.associateWith { stacks -> stacks.withIndex().filter { (_, value) -> value != null } }
		if (byInventory.isEmpty()) return

		val byCount = mutableMapOf<ItemStack, Int>()
		val itemReferences = multimapOf<ItemStack, ItemReference>()

		for ((inventory, items) in byInventory) {
			for ((index, item) in items) {
				val asOne = item.asOne()

				itemReferences[asOne].add(ItemReference(inventory, index))

				if (byCount.containsKey(asOne)) continue
				val count = items.sumOf { stack -> if (stack.value.isSimilar(item)) stack.value.amount else 0 }
				byCount[asOne] = count
			}
		}

//		debugAudience.information("counts: [${byCount.entries.joinToString { "${it.key.type}, ${it.value}]" }}, ${toVec3i(location)}")
//		debugAudience.information("references: [${itemReferences.entries().joinToString { "${it.key} ${it.value}" }}, ${toVec3i(location)}")

		val originNode = getOrCache(location) ?: return

		val destinationInvCache = mutableMapOf<BlockKey, Inventory>()

		for ((item, count) in byCount) 	transferItemType(
			location,
			originNode,
			meta,
			item,
			count,
			destinationInvCache,
			itemReferences
		)
	}

	fun transferItemType(
		originKey: BlockKey,
		originNode: Node,
		meta: ItemExtractorMetaData?,
		singletonItem: ItemStack,
		count: Int,
		destinationInvCache: MutableMap<BlockKey, Inventory>,
		itemReferences: Multimap<ItemStack, ItemReference>,
	) {
		val availableItemReferences = itemReferences[singletonItem]
//		debugAudience.information("Checking ${singletonItem.type} [$count]")

		val destinations: List<BlockKey> = getNetworkDestinations<ItemNode.InventoryNode>(originKey, originNode) { node ->
			val inventory = destinationInvCache.getOrPut(node.position) {
				getInventory(node.position) ?: return@getNetworkDestinations false
			}

			LegacyItemUtils.canFit(inventory, singletonItem, 1) && availableItemReferences.none { reference -> reference.inventory == inventory }
		}.toList()

//		if (destinations.isEmpty()) return debugAudience.information("No destinations found")

		val numDestinations = destinations.size

		val paths: Array<PathfindingReport?> = Array(numDestinations) {
			findPath(
				origin = Node.NodePositionData(
					ItemNode.ItemExtractorNode,
					holder.getWorld(),
					originKey,
					BlockFace.SELF
				),
				destination = destinations[it],
				ignoreCache = true // TODO wait for a caching implementation that will allow compound keys for item types
			) { node, blockFace ->
				if (node !is ItemNode.FilterNode) return@findPath true

				node.matches(singletonItem)
			}
		}

		val validDestinations = destinations.filterIndexedTo(mutableListOf()) { index, destination ->
			val path = paths[index]
			path != null
		}
		if (validDestinations.isEmpty()) return

//		meta?.markItemPathfind(item) TODO path caching for items

		val transaction = ItemTransaction()

		for (reference in availableItemReferences) {
			var destination = getDestination(meta, originKey, validDestinations)

//			debugAudience.information("Selected destination ${toVec3i(destination)}")

			val destinationInventory = destinationInvCache[destination]!!
			val room = LegacyItemUtils.getSpaceFor(destinationInventory, singletonItem)

			if (room == 0) {
				validDestinations.remove(destination)
				destination = getDestination(meta, originKey, validDestinations)
				continue
			}

			val amount = minOf(reference.get()?.amount ?: 0, room)
			if (amount == 0) continue

			transaction.addTransfer(
				reference,
				destinationInventory,
				singletonItem,
				amount
			)

//			debugAudience.highlightBlock(toVec3i(destination), 40L)
		}

		transaction.commit()
	}

	fun getDestination(meta: ItemExtractorMetaData?, extractorKey: BlockKey, destinations: List<BlockKey>): BlockKey {
		if (meta != null) {
			return meta.sortingOrder.getDestination(meta, destinations)
		}

		val extractorPosition = toVec3i(extractorKey)
		return destinations.minBy { key -> extractorPosition.distance(toVec3i(key)) }
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
