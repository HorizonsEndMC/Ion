package net.horizonsend.ion.server.features.transport.grid

import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.transport.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.grid.node.GridNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.minecraft.world.level.chunk.LevelChunkSection
import org.bukkit.block.Block
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractGrid(val network: ChunkTransportNetwork) {
	val nodes: ConcurrentHashMap<Long, GridNode> = ConcurrentHashMap()

	val grids: Nothing = TODO("Grid system")

	/**
	 *
	 **/
	abstract fun isNode(block: Block): Boolean

	// TODO maybe move this to a member function of grid node
	abstract fun shouldConsolidate(node: GridNode)

	/**
	 * Builds the grid TODO better documentation
	 **/
	fun build() = network.scope.launch {
		collectAllNodes().join()
		consolidateNodes()
		buildGraph()
	}

	/**
	 *
	 **/
	private fun collectAllNodes(): Job = network.scope.launch {
		// Parallel collect the nodes of each section
		network.chunk.sections.map { (y, section) ->
			launch { collectSectionNodes(y, section) }
		}.joinAll()
	}

	/**
	 * Collect all nodes in this chunk section
	 **/
	fun collectSectionNodes(sectionY: Int, section: LevelChunkSection) {
		for (x: Int in 0..15) for (y: Int in 0..15) for (z: Int in 0..15) {
			val key = toBlockKey(x, y, z)
		}
	}

	/**
	 * Consolidates network nodes where possible
	 *
	 * e.g. a straight section may be represented as a single node
	 **/
	fun consolidateNodes() {

	}

	/**
	 *
	 **/
	fun buildGraph() {

	}
}
