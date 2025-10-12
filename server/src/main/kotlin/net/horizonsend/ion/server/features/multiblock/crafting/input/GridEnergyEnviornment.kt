package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNetwork

interface GridEnergyEnviornment : RecipeEnviornment {
	val gridEnergyNetworkManager get() = multiblock.manager.getTransportManager().getGridEnergyGraphTransportManager()

	fun getConnectedNetworks(): Set<GridEnergyNetwork> = (multiblock as GridEnergyMultiblock).getConnectedNetworks()

	fun hasAvailablePower(amount: Double, threshold: Double = 1.0): Boolean {
		return getAvailablePower(amount) > threshold
	}

	fun getAvailablePower(amount: Double): Double {
		return getConnectedNetworks().maxOfOrNull { network -> network.hasAvailablePower(amount) } ?: 0.0
	}
}
