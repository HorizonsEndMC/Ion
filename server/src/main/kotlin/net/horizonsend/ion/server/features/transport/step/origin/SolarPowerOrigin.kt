package net.horizonsend.ion.server.features.transport.step.origin

import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode

class SolarPowerOrigin(val origin: SolarPanelNode) : StepOrigin<ChunkPowerNetwork>, PowerOrigin {
	override fun getTransferPower(destination: PoweredMultiblockEntity): Int {
		val room = destination.maxPower - destination.getPower()
		val power = origin.tickAndGetPower()

		// No max transfer limit for solar fields, as large ones would quickly overwhelm any limit
		return minOf(power, room)
	}
}
