package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.TRANSPORT_NETWORK_NODE_TYPE
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType

object TransportNetworkNodeTypeKeys : KeyRegistry<TransportNodeType<*>>(TRANSPORT_NETWORK_NODE_TYPE, TransportNodeType::class) {
	val FLUID_JUNCTION_REGULAR = registerKey("FLUID_JUNCTION_REGULAR")
	val FLUID_LINEAR_REGULAR = registerKey("FLUID_LINEAR_REGULAR")
	val FLUID_PORT = registerKey("FLUID_PORT")
}
