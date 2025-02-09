package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.features.transport.items.util.ItemTransaction
import net.horizonsend.ion.server.features.transport.items.util.getRemovableItems
import net.horizonsend.ion.server.features.transport.items.util.getTransferSpaceFor
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.util.PathCache
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.util.getBlockEntity
import net.horizonsend.ion.server.features.transport.util.getIdealPath
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
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest
import org.bukkit.inventory.ItemStack
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

class ItemTransportCache(override val holder: CacheHolder<ItemTransportCache>): TransportCache(holder) {
	override val type: CacheType = CacheType.ITEMS
	override val extractorNodeClass: KClass<out Node> = ItemNode.ItemExtractorNode::class

	override val pathCache: PathCache<MutableMap<ItemStack, Optional<PathfindingReport>>> = PathCache.keyed<ItemStack>(this)

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

	private fun transferItemType(
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
				origin = NodePositionData(
					ItemNode.ItemExtractorNode,
					holder.getWorld(),
					originKey,
					BlockFace.SELF,
					this
				),
				destination = destinations[it],
				itemStack = singletonItem,
			) { node, _ ->
				if (node !is ItemNode.FilterNode) return@findPath true

				node.matches(singletonItem)
			}
		}

		val validDestinations = destinations.filterIndexedTo(mutableListOf()) { index, _ ->
			val path = paths[index]
			path != null
		}

		if (validDestinations.isEmpty()) return

		val transaction = ItemTransaction()

		for (reference in availableItemReferences) {
			val destinationInventory = selectDestination(
				singletonItem,
				destinationInvCache,
				validDestinations,
				meta,
				originKey
			) ?: break // If no destinations could be found, it is likely that no more will be, since all transfers are of the same item

			val room = getTransferSpaceFor(destinationInventory, singletonItem)

			val amount = minOf(reference.get()?.amount ?: 0, room)
			if (amount == 0) continue

			transaction.addTransfer(
				reference,
				destinationInventory,
				singletonItem,
				amount
			)
		}

		Tasks.sync {
			transaction.commit()
		}
	}

	private fun selectDestination(
		singletonItem: ItemStack,
		destinationInvCache: MutableMap<BlockKey, CraftInventory>,
		validDestinations: MutableList<BlockKey>,
		meta: ItemExtractorMetaData?,
		extractorKey: BlockKey
	): CraftInventory? {
		var destination: CraftInventory? = null

		var remainingIterations = validDestinations.size
		while (destination == null && remainingIterations > 0) {
			remainingIterations--

			val newLocation: BlockKey = getDestination(meta, extractorKey, validDestinations)
			val destinationInventory = destinationInvCache[newLocation]

			if (destinationInventory == null) {
				validDestinations.remove(newLocation)
				if (validDestinations.isEmpty()) break

				continue
			}

			if (getTransferSpaceFor(destinationInventory, singletonItem) == 0) {
				validDestinations.remove(newLocation)
				if (validDestinations.isEmpty()) break

				continue
			}

			destination = destinationInventory
		}

		return destination
	}

	fun getDestination(meta: ItemExtractorMetaData?, extractorKey: BlockKey, destinations: List<BlockKey>): BlockKey {
		if (meta != null) {
			return meta.sortingOrder.getDestination(meta, destinations)
		}

		val extractorPosition = toVec3i(extractorKey)
		return destinations.minBy { key -> extractorPosition.distance(toVec3i(key)) }
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
			if (holder.getOrCacheGlobalNode(inventoryLocation) !is ItemNode.InventoryNode) continue
			val inv = getInventory(inventoryLocation) ?: continue
			if (inv.isEmpty) continue
			inventories.add(inv)
		}

		return inventories
	}

	fun findPath(
		origin: NodePositionData,
		destination: BlockKey,
		itemStack: ItemStack,
		pathfindingFilter: ((Node, BlockFace) -> Boolean)? = null
	): PathfindingReport? {
		val entry = pathCache.getOrCompute(origin.position, destination) { mutableMapOf() } ?: return null // Should not return null, but handle the possibility

		return entry.getOrPut(itemStack) {
			val path = runCatching { getIdealPath(origin, destination, holder.nodeCacherGetter, pathfindingFilter) }.getOrNull()
			if (path == null) return@getOrPut Optional.empty()

			val resistance = calculatePathResistance(path)
			Optional.of(PathfindingReport(path, resistance))
		}.getOrNull()
	}

	fun selectDestinationInventory(): Nothing = TODO()
}
