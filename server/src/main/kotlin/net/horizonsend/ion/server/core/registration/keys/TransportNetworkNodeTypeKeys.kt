package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.core.registration.keys.RegistryKeys.TRANSPORT_NETWORK_NODE_TYPE
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.FluidPort
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.FluidValve
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.RegularJunctionPipe
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.RegularLinearPipe
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.ReinforcedJunctionPipe
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.ReinforcedLinearPipe
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNode.GridEnergyJunction
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNode.GridEnergyPort

object TransportNetworkNodeTypeKeys : KeyRegistry<TransportNodeType<*>>(TRANSPORT_NETWORK_NODE_TYPE, TransportNodeType::class) {
	val FLUID_JUNCTION_REGULAR = registerTypedKey<TransportNodeType<RegularJunctionPipe>>("FLUID_JUNCTION_REGULAR")
	val FLUID_LINEAR_REGULAR = registerTypedKey<TransportNodeType<RegularLinearPipe>>("FLUID_LINEAR_REGULAR")
	val FLUID_JUNCTION_REINFORCED = registerTypedKey<TransportNodeType<ReinforcedJunctionPipe>>("FLUID_JUNCTION_REINFORCED")
	val FLUID_LINEAR_REINFORCED = registerTypedKey<TransportNodeType<ReinforcedLinearPipe>>("FLUID_LINEAR_REINFORCED")
	val FLUID_PORT = registerTypedKey<TransportNodeType<FluidPort>>("FLUID_PORT")
	val FLUID_VALVE = registerTypedKey<TransportNodeType<FluidValve>>("FLUID_VALVE")

	val GRID_ENERGY_PORT = registerTypedKey<TransportNodeType<GridEnergyPort>>("GRID_ENERGY_PORT")
	val GRID_ENERGY_JUNCTION = registerTypedKey<TransportNodeType<GridEnergyJunction>>("GRID_ENERGY_JUNCTION")
}
