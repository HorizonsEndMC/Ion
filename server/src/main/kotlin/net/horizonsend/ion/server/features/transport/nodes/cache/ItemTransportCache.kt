package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.TransportTask
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.features.transport.items.util.ItemTransaction
import net.horizonsend.ion.server.features.transport.items.util.getRemovableItems
import net.horizonsend.ion.server.features.transport.items.util.getTransferSpaceFor
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.nodes.util.MappedDestinationCache
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
import net.minecraft.world.level.block.state.properties.ChestType
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

class ItemTransportCache(override val holder: CacheHolder<ItemTransportCache>): TransportCache(holder), DestinationCacheHolder {
	override val type: CacheType = CacheType.ITEMS
	override val extractorNodeClass: KClass<out Node> = ItemNode.ItemExtractorNode::class
	override val destinationCache = MappedDestinationCache<ItemStack>(this)

	override fun tickExtractor(
		location: BlockKey,
		delta: Double,
		metaData: ExtractorMetaData?,
		index: Int,
		count: Int,
	) {
		NewTransport.runTask(location, holder.getWorld()) {
			handleExtractorTick(this, location, metaData as? ItemExtractorMetaData)
		}
	}

	fun handleExtractorTick(task: TransportTask, location: BlockKey, meta: ItemExtractorMetaData?) {
		val sources = getSources(location)
		if (sources.isEmpty()) {
			return
		}

		val references = mutableMapOf<ItemStack, ArrayDeque<ItemReference>>()

		for (inventory in sources) {
			if (task.isInterrupted()) return

			for ((index, item: ItemStack) in getRemovableItems(inventory)) {
				if (task.isInterrupted()) return

				references.getOrPut(item.asOne()) { ArrayDeque() }.add(ItemReference(inventory, index))
			}
		}

		val originNode = getOrCache(location) ?: return

		val destinationInvCache = mutableMapOf<BlockKey, CraftInventory>()

		for ((item, itemReferences) in references) {
			if (task.isInterrupted()) return

			transferItemType(
				task,
				location,
				originNode,
				meta,
				item,
				destinationInvCache,
				itemReferences
			)
		}
	}

	private fun getTransferDestinations(
		task: TransportTask,
		extractorLocation: BlockKey,
		extractorNode: Node,
		singletonItem: ItemStack,
		destinationInvCache: MutableMap<BlockKey, CraftInventory>,
		availableItemReferences: ArrayDeque<ItemReference>
	): Array<PathfindResult>? {
		val destinations: Array<PathfindResult> = getOrCacheNetworkDestinations<ItemNode.InventoryNode>(
			task = task,
			originPos = extractorLocation,
			originNode = extractorNode,
			retainFullPath = false,
			cachingFunction = { destinations ->
				destinationCache.set(extractorNode::class, singletonItem, extractorLocation, destinations)
			},
			cacheGetter = {
				destinationCache.get(extractorNode::class, singletonItem, extractorLocation)
			},
			pathfindingFilter = pathfindingFilter@{ intermediateNode, _ ->
				if (intermediateNode !is ItemNode.FilterNode) return@pathfindingFilter true

				intermediateNode.matches(singletonItem)
			},
			destinationCheck = destinationFilter@{ destinationTypeNode ->
				validateDestination(destinationTypeNode, singletonItem, availableItemReferences, destinationInvCache)
			}
		)

		if (destinations.isEmpty()) return null

		return destinations
	}

	private fun validateDestination(
		node: NodePositionData,
		singletonItem: ItemStack,
		availableItemReferences: ArrayDeque<ItemReference>,
		destinationInvCache: MutableMap<BlockKey, CraftInventory>,
	): Boolean {
		// If the inventory is missing / can't be loaded, return
		val destinationInventory = destinationInvCache.getOrPut(node.position) {
			getInventory(node.position) ?: return false
		}

		// If it can't fit a single item, don't use it
		if (!LegacyItemUtils.canFit(destinationInventory, singletonItem, 1)) {
			return false
		}

		// IF it is an origin inventory, don't use it
		return availableItemReferences.none { itemReference ->
			val referenceInventory = itemReference.inventory

			// Special handling of double chests
			if (destinationInventory is CraftInventoryDoubleChest && referenceInventory is CraftInventoryDoubleChest) {
				val leftMatches = (referenceInventory.leftSide as CraftInventory).inventory == (destinationInventory.leftSide as CraftInventory).inventory
				val rightMatches = (referenceInventory.rightSide as CraftInventory).inventory == (destinationInventory.rightSide as CraftInventory).inventory

				return@none leftMatches || rightMatches
			}

			referenceInventory.inventory == destinationInventory.inventory
		}
	}

	private fun transferItemType(
		task: TransportTask,
		originKey: BlockKey,
		originNode: Node,
		meta: ItemExtractorMetaData?,
		singletonItem: ItemStack,
		destinationInvCache: MutableMap<BlockKey, CraftInventory>,
		availableItemReferences: ArrayDeque<ItemReference>,
	) {
		val destinations: MutableList<PathfindResult> = getTransferDestinations(
			task = task,
			extractorLocation = originKey,
			extractorNode = originNode,
			singletonItem = singletonItem,
			destinationInvCache = destinationInvCache,
			availableItemReferences = availableItemReferences
		)?.toMutableList() ?: return

		val transaction = ItemTransaction()

		val destinationInventories: MutableMap<PathfindResult, CraftInventory> = getDestinations(
			task,
			singletonItem,
			destinationInvCache,
			destinations,
			meta
		)

		for (reference in availableItemReferences) {
			val remainingDestinations = destinationInventories.keys

			if (task.isInterrupted()) return
			val room = getTransferSpaceFor(destinationInventories.values, singletonItem)
			val amount = minOf(reference.get()?.amount ?: 0, room)

			if (amount == 0) continue

			transaction.addTransfer(
				reference,
				remainingDestinations,
				singletonItem,
				amount
			) {
				val destination = getDestination(meta, remainingDestinations)
				destination to destinationInvCache[destination.destinationPosition]!!
			}
		}

		if (!transaction.isEmpty() && IonServer.isEnabled) {
			Tasks.sync {
				if (task.isInterrupted()) return@sync
				transaction.commit()
			}
		}
	}

	private fun getDestinations(
		task: TransportTask,
		singletonItem: ItemStack,
		destinationInvCache: MutableMap<BlockKey, CraftInventory>,
		validDestinations: MutableList<PathfindResult>,
		meta: ItemExtractorMetaData?
	): MutableMap<PathfindResult, CraftInventory> {
		// Ordered map to preserve order
		val foundDestinationInventories = mutableMapOf<PathfindResult, CraftInventory>()

		while (validDestinations.isNotEmpty()) {
			if (task.isInterrupted()) return mutableMapOf()

			val destination: PathfindResult = getDestination(meta, validDestinations)
			var destinationInventory = destinationInvCache[destination.destinationPosition]

			if (destinationInventory == null) {
				val found = getInventory(destination.destinationPosition)
				if (found != null) {
					destinationInventory = found
					destinationInvCache[destination.destinationPosition] = found
				}
			}

			if (destinationInventory == null) {
				validDestinations.remove(destination)
				if (validDestinations.isEmpty()) break

				continue
			}

			if (getTransferSpaceFor(destinationInventory, singletonItem) == 0) {
				validDestinations.remove(destination)
				if (validDestinations.isEmpty()) break

				continue
			}

			foundDestinationInventories[destination] = destinationInventory
			validDestinations.remove(destination)
			if (validDestinations.isEmpty()) break
		}

		return foundDestinationInventories
	}

	fun getDestination(meta: ItemExtractorMetaData?, destinations: Collection<PathfindResult>): PathfindResult {
		if (meta != null) {
			return meta.sortingOrder.getDestination(meta, destinations)
		}

		return destinations.minBy { wrapper -> wrapper.trackedPath.length }
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

				val right = if (type == ChestType.RIGHT) entity else otherEntity
				val left = if (type == ChestType.LEFT) entity else otherEntity
				return CraftInventoryDoubleChest(CompoundContainer(right, left))
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
			if (holder.globalNodeCacher.invoke(this, holder.getWorld(), inventoryLocation)?.second !is ItemNode.InventoryNode) continue
			val inv = getInventory(inventoryLocation) ?: continue
			if (inv.isEmpty) continue
			inventories.add(inv)
		}

		return inventories
	}
}
