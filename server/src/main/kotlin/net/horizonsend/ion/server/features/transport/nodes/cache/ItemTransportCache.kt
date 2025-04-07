package net.horizonsend.ion.server.features.transport.nodes.cache

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.features.transport.items.util.ItemTransaction
import net.horizonsend.ion.server.features.transport.items.util.getRemovableItems
import net.horizonsend.ion.server.features.transport.items.util.getTransferSpaceFor
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.nodes.util.MappedDestinationCache
import net.horizonsend.ion.server.features.transport.nodes.util.PathfindingNodeWrapper
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
import java.util.function.Supplier
import kotlin.reflect.KClass

class ItemTransportCache(override val holder: CacheHolder<ItemTransportCache>): TransportCache(holder), DestinationCacheHolder {
	override val type: CacheType = CacheType.ITEMS
	override val extractorNodeClass: KClass<out Node> = ItemNode.ItemExtractorNode::class
	override val destinationCache = MappedDestinationCache<ItemStack>(this)

	override fun tickExtractor(
		location: BlockKey,
		delta: Double,
		metaData: ExtractorMetaData?,
	) {
		NewTransport.runTask {
			handleExtractorTick(location, metaData as? ItemExtractorMetaData)
		}
	}

	fun handleExtractorTick(location: BlockKey, meta: ItemExtractorMetaData?) {
		val sources = getSources(location)
		if (sources.isEmpty()) {
			return
		}

		val references = mutableMapOf<ItemStack, ArrayDeque<ItemReference>>()

		for (inventory in sources) {
			for ((index, item: ItemStack) in getRemovableItems(inventory)) {
				references.getOrPut(item.asOne()) { ArrayDeque() }.add(ItemReference(inventory, index))
			}
		}

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

	private fun getTransferDestinations(
		extractorLocation: BlockKey,
		extractorNode: Node,
		singletonItem: ItemStack,
		destinationInvCache: MutableMap<BlockKey, CraftInventory>,
		availableItemReferences: ArrayDeque<ItemReference>
	): Collection<PathfindingNodeWrapper>? {
		val destinations: Collection<PathfindingNodeWrapper> = getOrCacheNetworkDestinations<ItemNode.InventoryNode>(
			originPos = extractorLocation,
			originNode = extractorNode,
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
		originKey: BlockKey,
		originNode: Node,
		meta: ItemExtractorMetaData?,
		singletonItem: ItemStack,
		destinationInvCache: MutableMap<BlockKey, CraftInventory>,
		availableItemReferences: ArrayDeque<ItemReference>,
	) {
		val destinations: MutableList<PathfindingNodeWrapper> = getTransferDestinations(
			extractorLocation = originKey,
			extractorNode = originNode,
			singletonItem = singletonItem,
			destinationInvCache = destinationInvCache,
			availableItemReferences = availableItemReferences
		)?.toMutableList() ?: return

		val transaction = ItemTransaction()

		val destinationInventories = getDestinations(
			singletonItem,
			destinationInvCache,
			destinations,
			meta
		)

		for (reference in availableItemReferences) {
			val amount = Supplier {
				val room = getTransferSpaceFor(destinationInventories.values, singletonItem)
				minOf(reference.get()?.amount ?: 0, room)
			}

			if (amount.get() == 0) continue

			transaction.addTransfer(
				reference,
				destinationInventories,
				singletonItem,
				amount
			) { invs ->
				val destination = getDestination(meta, invs.keys)
				destination to invs[destination]!!
			}
		}

		if (!transaction.isEmpty()) {
			Tasks.sync {
				transaction.commit()
			}
		}
	}

	private fun getDestinations(
		singletonItem: ItemStack,
		destinationInvCache: MutableMap<BlockKey, CraftInventory>,
		validDestinations: MutableList<PathfindingNodeWrapper>,
		meta: ItemExtractorMetaData?
	): Object2ObjectRBTreeMap<PathfindingNodeWrapper, CraftInventory> {
		// Ordered map to preserve order
		val foundDestinationInventories = Object2ObjectRBTreeMap<PathfindingNodeWrapper, CraftInventory>()

		for (n in validDestinations.indices) {
			val destination: PathfindingNodeWrapper = getDestination(meta, validDestinations)
			var destinationInventory = destinationInvCache[destination.node.position]

			if (destinationInventory == null) {
				val found = getInventory(destination.node.position)
				if (found != null) {
					destinationInventory = found
					destinationInvCache.put(destination.node.position, found)
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

	fun getDestination(meta: ItemExtractorMetaData?, destinations: Collection<PathfindingNodeWrapper>): PathfindingNodeWrapper {
		if (meta != null) {
			return meta.sortingOrder.getDestination(meta, destinations)
		}

		return destinations.minBy { wrapper -> wrapper.depth }
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
			if (holder.globalGetter.invoke(this, holder.getWorld(), inventoryLocation)?.second !is ItemNode.InventoryNode) continue
			val inv = getInventory(inventoryLocation) ?: continue
			if (inv.isEmpty) continue
			inventories.add(inv)
		}

		return inventories
	}
}
