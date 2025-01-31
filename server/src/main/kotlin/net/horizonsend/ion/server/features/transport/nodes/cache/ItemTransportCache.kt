package net.horizonsend.ion.server.features.transport.nodes.cache

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
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.blockFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockSateSafe
import net.minecraft.world.CompoundContainer
import net.minecraft.world.Container
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.DoubleBlockCombiner
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.block.state.properties.ChestType
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest
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

		val references = mutableMapOf<ItemStack, ArrayDeque<ItemReference>>()

		for (inventory in sources) {
			val items = inventory.contents.withIndex()

			for ((index, item: ItemStack?) in items) {
				if (item == null) continue

				references.getOrPut(item.asOne()) { ArrayDeque() }.add(ItemReference(inventory, index))
			}
		}

//		debugAudience.information("references: [${references.entries.joinToString { "${it.key} ${it.value}" }}, ${toVec3i(location)}")

		val originNode = getOrCache(location) ?: return

		val destinationInvCache = mutableMapOf<BlockKey, CraftInventory>()

		for ((item, itemReferences) in references) transferItemType(
			location,
			originNode,
			meta,
			item,
			destinationInvCache,
			itemReferences
		)
	}

	fun transferItemType(
		originKey: BlockKey,
		originNode: Node,
		meta: ItemExtractorMetaData?,
		singletonItem: ItemStack,
		destinationInvCache: MutableMap<BlockKey, CraftInventory>,
		availableItemReferences: ArrayDeque<ItemReference>,
	) {
//		debugAudience.information("Checking ${singletonItem.type} [$count]")

		val destinations: List<BlockKey> = getNetworkDestinations<ItemNode.InventoryNode>(originKey, originNode) { node ->
			val destinationInventory = destinationInvCache.getOrPut(node.position) {
				getInventory(node.position) ?: return@getNetworkDestinations false
			}

			if (!LegacyItemUtils.canFit(destinationInventory, singletonItem, 1)) {
				return@getNetworkDestinations false
			}

			availableItemReferences.none { itemReference ->
				val referenceInventory = itemReference.inventory
				if (destinationInventory is CraftInventoryDoubleChest && referenceInventory is CraftInventoryDoubleChest) {
					val leftMatches = (referenceInventory.leftSide as CraftInventory).inventory == (destinationInventory.leftSide as CraftInventory).inventory
					val rightMatches = (referenceInventory.rightSide as CraftInventory).inventory == (destinationInventory.rightSide as CraftInventory).inventory

					return@none leftMatches || rightMatches
				}

				referenceInventory.inventory == destinationInventory.inventory
			}
		}.toList()

//		if (destinations.isEmpty()) return debugAudience.information("No destinations found")

		val paths: Array<PathfindingReport?> = Array(destinations.size) {
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

		Tasks.sync {
			transaction.commit()
		}
	}

	fun getDestination(meta: ItemExtractorMetaData?, extractorKey: BlockKey, destinations: List<BlockKey>): BlockKey {
		if (meta != null) {
			return meta.sortingOrder.getDestination(meta, destinations)
		}

		val extractorPosition = toVec3i(extractorKey)
		return destinations.minBy { key -> extractorPosition.distance(toVec3i(key)) }
	}

	companion object {
		private val CHEST_COMBINER = object  : DoubleBlockCombiner.Combiner<ChestBlockEntity, Int> {
			override fun acceptDouble(p0: ChestBlockEntity, p1: ChestBlockEntity): Int {
				return 2
			}

			override fun acceptSingle(p0: ChestBlockEntity): Int {
				return 1
			}

			override fun acceptNone(): Int {
				return 0
			}
		}
	}

	/**
	 * Gets a live inventory.
	 * RETURNED INVENTORIES SHOULD NOT BE MODIFIED ASYNC
	 **/
	fun getInventory(localKey: BlockKey): CraftInventory? {
		val global = holder.transportManager.getGlobalCoordinate(toVec3i(localKey))

		val state = getNMSBlockSateSafe(holder.getWorld(), global.x, global.y, global.z) ?: return null
		val entity = getBlockEntity(global, holder.getWorld()) as? Container ?: return null

		return when (state.block) {
			is ChestBlock -> {
				val type = state.getValue(ChestBlock.TYPE)

				if (type == ChestType.SINGLE) {
					return CraftInventory(entity)
				}

				val relativeFace = when (type) {
					// If it is on the left, need to look right, vice versa
					ChestType.LEFT -> RelativeFace.RIGHT
					ChestType.RIGHT -> RelativeFace.LEFT
					else -> error("Single condition already matched")
				}

				val direction = state.getValue(ChestBlock.FACING)
				val otherPos = global.getRelative(relativeFace[direction.blockFace])

				val otherEntity = getBlockEntity(otherPos, holder.getWorld()) as? Container

				if (otherEntity == null) {
					return CraftInventory(entity)
				}

				if (otherEntity == entity) {
					// Idk, just in case
					return CraftInventory(entity)
				}

				val left = if (type == ChestType.LEFT) entity else otherEntity
				val right = if (type == ChestType.RIGHT) entity else otherEntity
				return CraftInventoryDoubleChest(CompoundContainer(left, right))
			}
			else -> {
				CraftInventory(entity)
			}
		}
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
