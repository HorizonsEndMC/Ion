package net.horizonsend.ion.server.features.transport.manager.holders

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.filters.manager.FilterCache
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.World

abstract class CacheHolder<T: TransportCache>(open val transportManager: TransportManager<*>) {
	abstract val cache: T

	open fun markReady() {
		cache.markReady()
	}

	abstract fun getWorld(): World

	/**
	 * Builds the transportNetwork
	 *
	 * Existing data will be loaded from the chunk's persistent data container, relations between nodes will be built, and any finalization will be performed
	 **/
	open fun handleLoad() {}

	/**
	 * Logic for when the holder unloaded
	 **/
	open fun handleUnload() {}

	/**
	 * Get a node inside this network
	 **/
	abstract fun getInternalNode(key: BlockKey): Node?

	/**
	 * Method used to access nodes inside, and outside the network
	 **/
	abstract fun getOrCacheGlobalNode(key: BlockKey): Node?

	abstract fun getMultiblockManager(): MultiblockManager

	abstract fun getExtractorManager(): ExtractorManager

	abstract fun getFilterManager(): FilterCache

	abstract fun getInputManager(): InputManager

	abstract fun getCacheHolderAt(key: BlockKey): CacheHolder<T>?

	/** Gets the node at the specified location, caches if needed */
	abstract val globalNodeCacher: CacheProvider

	/** Gets the node at the specified location, does not cache */
	abstract val globalNodeLookup: CacheProvider

	abstract fun isLocal(key: BlockKey): Boolean
}

/**
 * A cache holder specific lookup. Used to differentiate ship and chunk caches.
 * TransportCache is the transport cache of whatever is performing the lookup. Passing this variable can eliminate costly chunk lookups.
 **/
typealias CacheProvider = (TransportCache, World, BlockKey) -> Pair<TransportCache, Node?>?
