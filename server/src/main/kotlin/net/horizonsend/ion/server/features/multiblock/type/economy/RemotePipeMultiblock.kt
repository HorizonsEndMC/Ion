package net.horizonsend.ion.server.features.multiblock.type.economy

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.transport.TransportTask
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.util.Path
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface RemotePipeMultiblock {
	fun getNetworkedExtractors(pipeSearchPoints: Array<Vec3i>): Map<BlockKey, Array<PathfindResult>> {
		this as MultiblockEntity

		val transportManager = manager.getTransportManager()
		val itemCacheHolder = transportManager.itemPipeManager

		val localPipeInputKeys = pipeSearchPoints.map { i -> toBlockKey(getPosRelative(i.x, i.y, i.z)) }

		val allDestinations = localPipeInputKeys.associateWith { inputLoc ->
			val cacheResult = itemCacheHolder.globalNodeCacher.invoke(itemCacheHolder.cache, world, inputLoc) ?: return@associateWith arrayOf()
			val node = cacheResult.second ?: return@associateWith arrayOf()

			val task = TransportTask(inputLoc, world, {}, 1000, IonServer.slF4JLogger)

			itemCacheHolder.cache.getNetworkDestinations(
				task = task,
				destinationTypeClass = ItemNode.ItemExtractorNode::class,
				originPos = inputLoc,
				originNode = node,
				retainFullPath = true,
				nextNodeProvider = { getPreviousNodes(itemCacheHolder.globalNodeCacher, null) }
			)
		}

		return allDestinations
	}

	fun getNetworkedInventories(extractors: Array<Vec3i>): Map<BlockKey, MutableSet<InventoryReference>> {
		this as MultiblockEntity

		val transportManager = manager.getTransportManager()
		val itemCacheHolder = transportManager.itemPipeManager

		val localPipeInputKeys = extractors.map { i -> toBlockKey(getPosRelative(i.x, i.y, i.z)) }

		val allDestinations: Map<BlockKey, MutableSet<InventoryReference>> = localPipeInputKeys.associateWith { inputLoc ->
			val cacheResult = itemCacheHolder.globalNodeCacher.invoke(itemCacheHolder.cache, world, inputLoc) ?: return@associateWith mutableSetOf()
			val node = cacheResult.second ?: return@associateWith mutableSetOf()

			val task = TransportTask(inputLoc, world, {}, 1000, IonServer.slF4JLogger)

			val destinations = itemCacheHolder.cache.getNetworkDestinations(
				task = task,
				destinationTypeClass = ItemNode.InventoryNode::class,
				originPos = inputLoc,
				originNode = node,
				retainFullPath = true,
				nextNodeProvider = { getNextNodes(cachedNodeProvider = itemCacheHolder.globalNodeCacher, filter = { _, _ -> true }) }
			)

			destinations.mapNotNullTo(mutableSetOf()) { pathResult ->
				val inventory = itemCacheHolder.cache.getInventory(pathResult.destinationPosition) ?: return@mapNotNullTo null

				InventoryReference.RemoteInventoryReference(inventory, itemCacheHolder, pathResult.trackedPath)
			}
		}

		return allDestinations
	}

	fun getRemoteReferences(extractors: Map<BlockKey, Array<PathfindResult>>, itemCache: ItemTransportCache): Set<InventoryReference.RemoteInventoryReference> {
		return extractors.flatMapTo(mutableSetOf()) { (_, destinations) ->

			destinations.flatMap { pathResult: PathfindResult ->
				itemCache.getSources(pathResult.destinationPosition).map {
					InventoryReference.RemoteInventoryReference(it, itemCache.holder, pathResult.trackedPath)
				}
			}
		}
	}

	fun getStandardReference(offset: Vec3i): InventoryReference.StandardInventoryReference? {
		this as MultiblockEntity

		val transportManager = manager.getTransportManager()
		val itemCache = transportManager.itemPipeManager.cache

		val inv = itemCache.getInventory(toBlockKey(getPosRelative(right = offset.x, up = offset.y, forward = offset.z))) ?: return null
		return InventoryReference.StandardInventoryReference(inv)
	}

	sealed interface InventoryReference {
		val inventory: CraftInventory
		fun isAvailable(itemStack: ItemStack): Boolean

		companion object {
			fun wrap(inventory: Inventory) = StandardInventoryReference(inventory as CraftInventory)
		}

		data class StandardInventoryReference(override val inventory: CraftInventory): InventoryReference {
			override fun isAvailable(itemStack: ItemStack): Boolean = true
		}

		data class RemoteInventoryReference(
			override val inventory: CraftInventory,
			val originCache: CacheHolder<ItemTransportCache>,
			val path: Path
		): InventoryReference {
			override fun isAvailable(itemStack: ItemStack): Boolean = path.isValid(originCache)
		}
	}
}
