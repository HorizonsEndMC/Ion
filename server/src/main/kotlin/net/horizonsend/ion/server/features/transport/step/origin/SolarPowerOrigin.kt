package net.horizonsend.ion.server.features.transport.step.origin

import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode

class SolarPowerOrigin(val origin: SolarPanelNode) : StepOrigin<ChunkPowerNetwork>, PowerOrigin {
	override val transferLimit: Int = Int.MAX_VALUE
	private val powerTransfer = origin.tickAndGetPower()

	override fun getTransferPower(destination: PoweredMultiblockEntity): Int {
		val destinationCapacity = destination.maxPower - destination.getPower()

		// No max transfer limit for solar fields, as large ones would quickly overwhelm any limit
		return minOf(powerTransfer, destinationCapacity)
	}
}
