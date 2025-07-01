package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeGraph
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Material
import kotlin.properties.Delegates

interface FluidNode : GraphNode {
	val volume: Double
	val graph: FluidGraph

	class RegularPipe(override val location: BlockKey) : FluidNode {
		override var graph: FluidGraph by Delegates.notNull<FluidGraph>(); private set
		override val volume: Double = 100.0

		override fun isIntact(): Boolean? {
			val world = graph.manager.transportManager.getWorld()
			val globalVec3i = graph.manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.type == Material.COPPER_GRATE
		}

		override fun setGraph(graph: TransportNodeGraph<*>) {
			this.graph = graph as FluidGraph
		}

		override fun getGraph(): TransportNodeGraph<*> {
			return graph
		}
	}

	class Input(override val location: BlockKey) : FluidNode {
		override var graph: FluidGraph by Delegates.notNull<FluidGraph>(); private set
		override val volume: Double = 100.0

		override fun isIntact(): Boolean? {
			val world = graph.manager.transportManager.getWorld()
			val globalVec3i = graph.manager.transportManager.getGlobalCoordinate(toVec3i(location))
			val block = getBlockIfLoaded(world, globalVec3i.x, globalVec3i.y, globalVec3i.z) ?: return null

			return block.type == Material.FLETCHING_TABLE
		}

		override fun setGraph(graph: TransportNodeGraph<*>) {
			this.graph = graph as FluidGraph
		}

		override fun getGraph(): TransportNodeGraph<*> {
			return graph
		}
	}
}
