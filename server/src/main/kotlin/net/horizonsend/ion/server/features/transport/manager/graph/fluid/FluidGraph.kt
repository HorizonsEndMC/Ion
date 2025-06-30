package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.inputs.InputType
import net.horizonsend.ion.server.features.transport.manager.graph.GraphManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeGraph
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.Input
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Material
import java.util.UUID

@Suppress("UnstableApiUsage")
class FluidGraph(uuid: UUID, override val manager: GraphManager<*, *>) : TransportNodeGraph<FluidNode>(uuid, manager) {
	override val cacheFactory: BlockBasedCacheFactory<FluidNode, TransportNodeGraph<FluidNode>> = BlockBasedCacheFactory.builder<FluidNode, TransportNodeGraph<FluidNode>>()
		.addSimpleNode(Material.COPPER_GRATE) { pos, _, holder -> FluidNode.RegularPipe(pos) }
		.addSimpleNode(Material.FLETCHING_TABLE) { pos, _, holder -> FluidNode.Input(pos) }
		.build()

	override fun getEdge(
		nodeOne: FluidNode,
		nodeTwo: FluidNode,
	): GraphEdge {
		return object : GraphEdge {
			override val nodeOne: GraphNode = nodeOne
			override val nodeTwo: GraphNode = nodeTwo
		}
	}

	val ports = mutableSetOf<Input>()

	var contents: FluidStack = FluidStack.empty()
	var cachedVolume: Double? = null

	fun getVolume(): Double {
		if (cachedVolume != null) return cachedVolume!!

		val new = networkGraph.nodes().sumOf { it.volume }
		cachedVolume = new
		return new
	}

	override fun onNodeAdded(new: FluidNode) {
		if (new is Input) ports.add(new)

		cachedVolume = null
	}

	fun depositToNetwork(inputLocation: BlockKey, multiblock: FluidStoringMultiblock) {
		var remainingRoom = maxOf(0.0, getVolume() - contents.amount)
		if (remainingRoom <= 0.0) return

		val atLocation = getOrCache(inputLocation)
		if (atLocation !is Input) return

		for (store in multiblock) {
			val toRemove = minOf(remainingRoom, store.getContents().amount, MAX_REMOVE_AMOUNT_PER_TICK)
			val notRemoved = store.removeAmount(toRemove)
			contents.amount += (toRemove - notRemoved)
		}

//		println("contents: ${multiblock}")
	}

	companion object {
		private const val MAX_REMOVE_AMOUNT_PER_TICK = 100.0
	}

	fun addToMultiblocks() {
		fun depositToEntities(input: Input) {
			val holders = manager.transportManager.getInputProvider().getHolders(InputType.FLUID, input.location)
		}

		for (input in ports) {
			depositToEntities(input)
		}
	}

	override fun tick() {
		val volume = cachedVolume ?: getVolume()

		if (contents.amount > volume) {
			contents.amount = volume
		}

		addToMultiblocks()
	}
}
