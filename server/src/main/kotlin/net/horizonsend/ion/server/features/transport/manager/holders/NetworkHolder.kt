package net.horizonsend.ion.server.features.transport.manager.holders

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.cache.CachedNode
import net.horizonsend.ion.server.features.transport.cache.TransportCache
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.World

interface NetworkHolder <T: TransportCache> {
	val network: T

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
	fun getInternalNode(key: BlockKey): CachedNode?

	/**
	 * Method used to access nodes inside, and outside the network
	 **/
	fun getOrCacheGlobalNode(key: BlockKey): CachedNode?

	fun getMultiblockManager(): MultiblockManager

	fun getExtractorManager(): ExtractorManager
}
