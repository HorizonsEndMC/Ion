package net.horizonsend.ion.server.features.transport.network.holders

import kotlinx.coroutines.CoroutineScope
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.world.chunk.ChunkRegion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.World
import kotlin.properties.Delegates

class ShipNetworkHolder<T: TransportNetwork>(val starship: ActiveStarship) : NetworkHolder<T> {
	override var network: T by Delegates.notNull(); private set

	constructor(manager: ActiveStarship, network: (ShipNetworkHolder<T>) -> T) : this(manager) {
		this.network = network(this)
	}

	override val scope: CoroutineScope = ChunkRegion.scope

	override fun getWorld(): World = starship.world

	override fun handleLoad() {
		TODO("Not yet implemented")
	}

	override fun handleUnload() {
		TODO("Not yet implemented")
	}

	override fun getInternalNode(key: BlockKey): TransportNode? {
		return network.nodes[key]
	}

	override fun getGlobalNode(key: BlockKey): TransportNode? {
		// Ship networks cannot access the outside world
		return getInternalNode(key)
	}

	fun captureNodes() {

	}
}
