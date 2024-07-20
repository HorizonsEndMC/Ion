package net.horizonsend.ion.server.features.transport.step.origin.power

import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

class SolarPowerOrigin(val origin: SolarPanelNode) : StepOrigin<PowerNetwork>, PowerOrigin {
	override val transferLimit: Int = Int.MAX_VALUE
	private val powerTransfer = origin.tickAndGetPower()

	override fun getTransferPower(destination: PoweredMultiblockEntity): Int {
		val destinationCapacity = destination.maxPower - destination.getPower()

		// No max transfer limit for solar fields, as large ones would quickly overwhelm any limit
		return minOf(powerTransfer, destinationCapacity)
	}
}
