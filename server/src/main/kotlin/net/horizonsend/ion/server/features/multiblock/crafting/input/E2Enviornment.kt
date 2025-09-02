package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.e2.E2Multiblock
import net.horizonsend.ion.server.features.transport.manager.graph.e2.E2Network

interface E2Enviornment : RecipeEnviornment {
	val e2NetworkManager get() = multiblock.manager.getTransportManager().getE2GraphTransportManager()

	fun getConnectedNetworks(): Set<E2Network> = (multiblock as E2Multiblock).getConnectedNetworks()

	fun hasAvailablePower(amount: Double, threshold: Double = 1.0): Boolean {
		return getAvailablePower(amount) > threshold
	}

	fun getAvailablePower(amount: Double): Double {
		(multiblock as E2Multiblock).setActiveE2Consumption(amount)
		return getConnectedNetworks().maxOfOrNull { network -> network.reCalculatePower() } ?: 0.0
	}
}
