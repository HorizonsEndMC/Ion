package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.server.command.misc.TransportDebugCommand
import net.horizonsend.ion.server.command.misc.TransportDebugCommand.measureOrFallback
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.items.SortingOrder
import net.horizonsend.ion.server.features.transport.items.transaction.Change
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
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.minecraft.world.Container
import net.minecraft.world.level.block.entity.BlockEntity
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
		val distributionOrder = meta?.sortingOrder ?: SortingOrder.NEAREST_FIRST

		val sources = getSources(location)
		if (sources.isEmpty()) {
			return
		}

		val byInventory = sources.associateWith { stacks -> stacks.filterNotNull() }
		if (byInventory.isEmpty()) return

		val byCount = mutableMapOf<ItemStack, Int>()
		val itemReferences = multimapOf<ItemStack, Pair<Inventory, ItemStack>>()

		for ((inventory, items) in byInventory) {
			for (item in items) {
				val asOne = item.asOne()
				if (byCount.containsKey(asOne)) continue
				val count = items.sumOf { stack -> if (stack.isSimilar(item)) stack.amount else 0 }
				byCount[asOne] = count
			}
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
