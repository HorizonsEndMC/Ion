package net.horizonsend.ion.server.features.transport.grid

import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.grid.sink.PowerSource
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode

class PowerGrid(manager: WorldGridManager) : Grid<PowerSource, PoweredMultiblockEntity>(GridType.Power, manager, PowerSource::class, PoweredMultiblockEntity::class) {
	override fun transferResources(from: PowerSource, to: PoweredMultiblockEntity, resistanceContribution: Int, totalResistance: Int) {
		val share = resistanceContribution.toDouble() / totalResistance.toDouble()

		when (from) {
			is PowerExtractorNode -> {
				val transferPower = from.getTransferPower() * share

			}

			is SolarPanelNode -> {

			}
		}
	}

	override fun postMerge(other: Grid<PowerSource, PoweredMultiblockEntity>) {
//		TODO("Not yet implemented")
	}

	override fun postSplit(new: List<Grid<PowerSource, PoweredMultiblockEntity>>) {
//		TODO("Not yet implemented")
	}
}
