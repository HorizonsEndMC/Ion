package net.horizonsend.ion.server.features.transport.node.manager

import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder

abstract class TransportManager {
	abstract val powerNodeManager: NetworkHolder<PowerNodeManager>
	abstract val fluidNodeManager: NetworkHolder<FluidNodeManager>

	fun tick() {
		powerNodeManager.network.tickTransport()
	}
}
