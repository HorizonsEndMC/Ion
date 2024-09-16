package net.horizonsend.ion.server.features.transport.node.manager

import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.manager.node.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.manager.node.PowerNodeManager

abstract class TransportManager {
	abstract val powerNodeManager: NetworkHolder<PowerNodeManager>
	abstract val fluidNodeManager: NetworkHolder<FluidNodeManager>

	fun tick() {

	}
}
