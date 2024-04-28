package net.horizonsend.ion.server.features.transport.grid

import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.Consolidatable
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import java.util.concurrent.ConcurrentHashMap

abstract class Grid(val network: ChunkTransportNetwork) {
	val nodes: ConcurrentHashMap<Long, TransportNode> = ConcurrentHashMap()
	val world get() = network.chunk.world

//	val grids: Nothing = TODO("Grid system")

	/**
	 *
	 **/
	open fun setup() {}

	/**
	 * Handle the creation / loading of the node into memory
	 *
	 * Inheritors may choose to save persistent data, or not
	 **/
	abstract fun loadNode(block: BlockSnapshot)

	abstract fun processBlockRemoval(key: Long)
	abstract fun processBlockAddition(key: Long, new: BlockSnapshot)

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
		finalizeNodes()
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

					loadNode(snapshot)
				}
			}
		}
	}

	/**
	 * Get the neighbors of a node
	 **/
	private fun collectNeighbors() {
//		nodes.values.forEach { node -> node.collectNeighbors() }
	}

	/**
	 * Consolidates network nodes where possible
	 *
	 * e.g. a straight section may be represented as a single node
	 **/
	private fun finalizeNodes() {
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

	fun getNode(x: Int, y: Int, z: Int): TransportNode? {
		val key = toBlockKey(x, y, z)
		return nodes[key]
	}
}
