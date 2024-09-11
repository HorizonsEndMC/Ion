package net.horizonsend.ion.server.features.transport.grid

import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import java.lang.IndexOutOfBoundsException
import java.util.concurrent.ConcurrentHashMap

class WorldGridManager(val world: IonWorld) {
	val allGrids = ConcurrentHashMap.newKeySet<Grid>()
	private val byType: ConcurrentHashMap<GridType, ConcurrentHashMap.KeySetView<Grid, Boolean>> = ConcurrentHashMap()

	private fun getGridsOfType(type: GridType): MutableSet<Grid> {
		return byType.getOrPut(type) { ConcurrentHashMap.newKeySet() }
	}

	fun tickSafely() {
		try {
		    tickTransport()
		} catch (e: Throwable) {
			return
		}
	}

	private fun tickTransport() {
		allGrids.forEach { it.tickTransport() }
	}

	fun registerGrid(grid: Grid) {
		allGrids.add(grid)
		getGridsOfType(grid.type).add(grid)
	}

	fun removeGrid(grid: Grid) {
		allGrids.remove(grid)
		getGridsOfType(grid.type).remove(grid)
	}

	@Suppress("UnstableApiUsage")
	fun joinOrCreateGrid(node: TransportNode): Grid {
		val relatedGrids = node.relationships.associateWithNotNull {
			val sideTwoNode = it.sideTwo.node
			if (!sideTwoNode.hasJoinedGrid()) return@associateWithNotNull null
			sideTwoNode.grid
		}

		return when (relatedGrids.entries.size) {
			0 -> createGrid(node)

			1 -> {
				val (neighbor, grid) = relatedGrids.entries.first()
				grid.addNode(node)
				grid.graph.putEdge(neighbor.sideTwo.node, node)

				grid
			}

			in 2..Int.MAX_VALUE -> {
				val new = mergeGrids(relatedGrids.values.toSet())
				for (neighbor in relatedGrids.keys) {
					new.graph.putEdge(neighbor.sideTwo.node, node)
				}

				new
			}

			else -> throw IllegalArgumentException("Negative number of grids?")
		}
	}

	/**
	 * Combines the provided grid, and returns the resulting combination
	 **/
	fun <T: Grid> mergeGrids(others: Set<T>): T {
		when (others.size) {
			0 -> throw IndexOutOfBoundsException("Attempted to merge 0 grids!")
			1 -> return others.first()
		}

		val largest = others.maxBy { it.nodes.size }

		for (grid in others.minus(largest)) {
			largest.handleMerge(grid)

			removeGrid(grid)
		}

		return largest
	}

	fun <T: Grid> splitGrid(grid: T): List<T> {
		throw NotImplementedError()
	}

	fun createGrid(origin: TransportNode): Grid {
		val newGrid = origin.gridType.newInstance(this)
		newGrid.addNode(origin)
		getGridsOfType(origin.gridType).add(newGrid)

		registerGrid(newGrid)

		return newGrid
	}
}
