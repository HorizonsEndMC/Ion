package net.horizonsend.ion.server.features.transport.grid.power

import com.google.common.graph.ElementOrder
import net.horizonsend.ion.server.features.transport.ExtractorData
import net.horizonsend.ion.server.features.transport.grid.AbstractGrid

class PowerGrid(private val extractorData: ExtractorData) : AbstractGrid<PowerNode>() {
	override fun isDirected(): Boolean = false
	override fun allowsSelfLoops(): Boolean = false

	override fun successors(node: PowerNode): MutableSet<PowerNode> {
		TODO("Not yet implemented")
	}

	override fun predecessors(node: PowerNode): MutableSet<PowerNode> {
		TODO("Not yet implemented")
	}

	override fun nodes(): MutableSet<PowerNode> {
		TODO("Not yet implemented")
	}

	override fun nodeOrder(): ElementOrder<PowerNode> {
		TODO("Not yet implemented")
	}

	override fun adjacentNodes(node: PowerNode): MutableSet<PowerNode> {
		return node.neighbors.values.mapTo(mutableSetOf()) { it as PowerNode }
	}
}
