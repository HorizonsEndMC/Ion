package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.TRANSPORT_NETWORK_NODE_TYPE
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.FluidPort
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.RegularJunctionPipe
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.RegularLinearPipe

object TransportNetworkNodeTypeKeys : KeyRegistry<TransportNodeType<*>>(TRANSPORT_NETWORK_NODE_TYPE, TransportNodeType::class) {
	val FLUID_JUNCTION_REGULAR = registerTypedKey<TransportNodeType<RegularJunctionPipe>>("FLUID_JUNCTION_REGULAR")
	val FLUID_LINEAR_REGULAR = registerTypedKey<TransportNodeType<RegularLinearPipe>>("FLUID_LINEAR_REGULAR")
	val FLUID_PORT = registerTypedKey<TransportNodeType<FluidPort>>("FLUID_PORT")
}
