package net.horizonsend.ion.server.features.transport.grid

sealed class GridType {
	abstract fun newInstance(manager: WorldGridManager): Grid<*, *>

	data object Fluid : GridType() {
		override fun newInstance(manager: WorldGridManager): FluidGrid {
			return FluidGrid(manager)
		}
	}

	data object Power : GridType() {
		override fun newInstance(manager: WorldGridManager): PowerGrid {
			return PowerGrid(manager)
		}
	}
}
