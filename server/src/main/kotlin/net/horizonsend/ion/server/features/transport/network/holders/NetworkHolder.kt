package net.horizonsend.ion.server.features.transport.network.holders

import kotlinx.coroutines.CoroutineScope
import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.World

interface NetworkHolder <T: TransportNetwork> {
	val network: T
	val scope: CoroutineScope

	fun getWorld(): World

	/**
	 * Builds the transportNetwork
	 *
	 * Existing data will be loaded from the chunk's persistent data container, relations between nodes will be built, and any finalization will be performed
	 **/
	fun handleLoad()

	/**
	 * Logic for when the holder unloaded
	 **/
	fun handleUnload()

	/**
	 * Get a node inside this network
	 **/
	fun getInternalNode(key: BlockKey): TransportNode?

	/**
	 * Method used to access nodes inside, and outside the network
	 **/
	fun getGlobalNode(key: BlockKey): TransportNode?
}