package net.horizonsend.ion.server.features.transport.node.manager.holders

import kotlinx.coroutines.CoroutineScope
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.features.transport.node.manager.ShipTransportManager
import net.horizonsend.ion.server.features.world.chunk.ChunkRegion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.World
import kotlin.properties.Delegates

class ShipNetworkHolder<T: NodeManager<*>>(val manager: ShipTransportManager) : NetworkHolder<T> {
	override var network: T by Delegates.notNull(); private set

	constructor(manager: ShipTransportManager, network: (ShipNetworkHolder<T>) -> T) : this(manager) {
		this.network = network(this)
	}

	override val scope: CoroutineScope = ChunkRegion.scope

	override fun getWorld(): World = manager.starship.world

	override fun handleLoad() {
		captureNodes()
	}

	override fun handleUnload() {
		network.clearData()
		rebuildStaticNetwork()
	}

	override fun getInternalNode(key: BlockKey): TransportNode? {
		return network.nodes[key]
	}

	override fun getGlobalNode(key: BlockKey): TransportNode? {
		// Ship networks cannot access the outside world
		return getInternalNode(key)
	}

	/**
	 * Build the transport network from the starship blocks
	 **/
	fun captureNodes() {

	}

	/**
	 * Rebuild the base networks in the chunks the ship occupies when it is released
	 **/
	fun rebuildStaticNetwork() {

	}

	override fun markUnsaved() {/* Do nothing */}

	override fun getMultiblockManager(): MultiblockManager {
		return manager.starship.multiblockManager
	}
}
