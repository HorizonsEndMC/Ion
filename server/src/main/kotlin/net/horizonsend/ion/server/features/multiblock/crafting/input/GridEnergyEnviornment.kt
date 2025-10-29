package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyPortMetaData
import net.horizonsend.ion.server.features.transport.inputs.IOPort.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNetwork
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

interface GridEnergyEnviornment : RecipeEnviornment {
	val powerInputs: List<RegisteredMetaDataInput<GridEnergyPortMetaData>>

	val gridEnergyNetworkManager get() = multiblock.manager.getTransportManager().getGridEnergyGraphTransportManager()

	fun getConnectedNetworks(): Map<BlockKey, GridEnergyNetwork> = (multiblock as GridEnergyMultiblock).getConnectedNetworks()

	fun hasAvailablePower(amount: Double, threshold: Double = 1.0): Boolean {
		return getAvailablePower(amount) > threshold
	}

	fun getAvailablePower(amount: Double): Double {
		return getConnectedNetworks().maxOfOrNull { (location, network) -> network.getAvailablePowerPercentage(location, amount) } ?: 0.0
	}
}
