package net.horizonsend.ion.server.features.transport.manager.holders

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.World

interface CacheHolder<T: TransportCache> {
	val transportManager: TransportManager<*>
	val cache: T

	fun getWorld(): World

	/**
	 * Builds the transportNetwork
	 *
	 * Existing data will be loaded from the chunk's persistent data container, relations between nodes will be built, and any finalization will be performed
	 **/
	fun handleLoad() {}

	/**
	 * Logic for when the holder unloaded
	 **/
	fun handleUnload() {}

	/**
	 * Get a node inside this network
	 **/
	fun getInternalNode(key: BlockKey): Node?

	/**
	 * Method used to access nodes inside, and outside the network
	 **/
	fun getOrCacheGlobalNode(key: BlockKey): Node?

	fun getMultiblockManager(): MultiblockManager

	fun getExtractorManager(): ExtractorManager

	fun getInputManager(): InputManager

	fun getCacheHolderAt(key: BlockKey): CacheHolder<T>?

	/** Gets the node at the specified location, caches if needed */
	val nodeCacherGetter: CacheProvider

	/** Gets the node at the specified location, does not cache */
	val cachedNodeLookup: CacheProvider
}

/**
 * A cache holder specific lookup. Used to differentiate ship and chunk caches.
 * TransportCache is the transport cache of whatever is performing the lookup. Passing this variable can eliminate costly chunk lookups.
 **/
typealias CacheProvider = (TransportCache, CacheType, World, BlockKey) -> Pair<TransportCache, Node?>?
