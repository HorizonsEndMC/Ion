package net.horizonsend.ion.server.features.transport.grid

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.transport.grid.sink.FluidSource

class FluidGrid(manager: WorldGridManager) : Grid<FluidSource, FluidStoringEntity>(GridType.Power, manager, FluidSource::class, FluidStoringEntity::class) {
	override fun transferResources(from: FluidSource, to: FluidStoringEntity, resistanceContribution: Int, totalResistance: Int) {
//		TODO("Not yet implemented")
	}

	override fun postMerge(other: Grid<FluidSource, FluidStoringEntity>) {
//		TODO("Not yet implemented")
	}

	override fun postSplit(new: List<Grid<FluidSource, FluidStoringEntity>>) {
//		TODO("Not yet implemented")
	}
}
