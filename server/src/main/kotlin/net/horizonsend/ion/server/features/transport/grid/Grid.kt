package net.horizonsend.ion.server.features.transport.grid

import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.grid.node.Consolidatable
import net.horizonsend.ion.server.features.transport.grid.node.GridNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import java.util.concurrent.ConcurrentHashMap

abstract class Grid(val network: ChunkTransportNetwork) {
	val nodes: ConcurrentHashMap<Long, GridNode> = ConcurrentHashMap()
	val world get() = network.chunk.world

	val grids: Nothing = TODO("Grid system")

	/**
	 *
	 **/
	open fun setup() {}

	/**
	 *
	 **/
	abstract fun isNode(block: BlockSnapshot): Boolean

	/**
	 * Handle the creation / loading of the node into memory
	 *
	 * Inheritors may choose to save persistent data, or not
	 **/
	abstract fun loadNode(block: BlockSnapshot): GridNode?

	abstract fun processBlockChange(previous: BlockSnapshot, new: BlockSnapshot)

	/**
	 *
	 **/
	abstract fun tick()

	/**
	 * Builds the grid TODO better documentation
	 **/
	fun build() = network.scope.launch {
		collectAllNodes().join()
		collectNeighbors()
		consolidateNodes()
		buildGraph()
	}

	/**
	 *
	 **/
	private fun collectAllNodes(): Job = network.scope.launch {
		// Parallel collect the nodes of each section
		network.chunk.sections.map { (y, _) ->
			launch { collectSectionNodes(y) }
		}.joinAll()
	}

	/**
	 * Collect all nodes in this chunk section
	 *
	 * Iterate the section for possible nodes, handle creation
	 **/
	suspend fun collectSectionNodes(sectionY: Int) {
		val originX = network.chunk.originX
		val originY = sectionY.shl(4) - network.chunk.inner.world.minHeight
		val originZ = network.chunk.originZ

		for (x: Int in 0..15) {
			val realX = originX + x

			for (y: Int in 0..15) {
				val realY = originY + y

				for (z: Int in 0..15) {
					val realZ = originZ + z

					val snapshot = getBlockSnapshotAsync(network.chunk.world, realX, realY, realZ) ?: continue

					if (!isNode(snapshot)) continue

					val node = loadNode(snapshot) ?: continue

					nodes[toBlockKey(realX, realY, realZ)] = node
				}
			}
		}
	}

	/**
	 * Get the neighbors of a node
	 **/
	private fun collectNeighbors() {
		nodes.values.forEach { node -> node.collectNeighbors() }
	}

	/**
	 * Consolidates network nodes where possible
	 *
	 * e.g. a straight section may be represented as a single node
	 **/
	private fun consolidateNodes() {
		nodes.forEach { (_, node) ->
			if (node is Consolidatable) node.consolidate()
		}
	}

	/**
	 *
	 **/
	private fun buildGraph() {
		//TODO
	}

	fun getNode(x: Int, y: Int, z: Int): GridNode? {
		val key = toBlockKey(x, y, z)
		return nodes[key]
	}
}
