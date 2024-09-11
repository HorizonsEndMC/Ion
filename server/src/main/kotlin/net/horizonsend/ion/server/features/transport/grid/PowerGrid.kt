package net.horizonsend.ion.server.features.transport.grid

import net.horizonsend.ion.server.features.transport.grid.util.Sink
import net.horizonsend.ion.server.features.transport.grid.util.Source

class PowerGrid(manager: WorldGridManager) : Grid(GridType.Power, manager) {
	override fun transferResources(from: Source, to: Sink, resistanceContribution: Int, totalResistance: Int) {
//		TODO("Not yet implemented")
	}

	override fun postMerge(other: Grid) {
//		TODO("Not yet implemented")
	}

	override fun postSplit(new: List<Grid>) {
//		TODO("Not yet implemented")
	}
}
