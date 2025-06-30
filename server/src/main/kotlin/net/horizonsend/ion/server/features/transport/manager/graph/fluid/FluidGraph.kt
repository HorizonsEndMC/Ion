package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.inputs.InputType
import net.horizonsend.ion.server.features.transport.manager.graph.FluidGraphManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeGraph
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.Input
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import java.util.UUID

class FluidGraph(uuid: UUID, override val manager: FluidGraphManager) : TransportNodeGraph<FluidNode>(uuid, manager) {
	override fun createEdge(
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

	@Synchronized
	fun getVolume(): Double {
		if (cachedVolume != null) return cachedVolume!!

		val new = getGraphNodes().sumOf { it.volume }
		cachedVolume = new
		return new
	}

	override fun onModified() {
		cachedVolume = null
	}

	fun depositToNetwork(inputLocation: BlockKey, multiblock: FluidStoringMultiblock) {
		var remainingRoom = maxOf(0.0, getVolume() - contents.amount)
		if (remainingRoom <= 0.0) return

		val removable = multiblock.getRemovable()
		if (removable.isEmpty()) return

		var type = contents.type

		for (storage in removable) {
			if (storage.getContents().isEmpty()) continue

			// Storage not empty, but pipe network is
			if (contents.isEmpty()) {
				type = storage.getContents().type
				contents.type = type
			}

			if (storage.getContents().type != type) continue

			val toRemove = minOf((getVolume() - contents.amount), storage.getContents().amount, 5.0)
			val notRemoved = storage.removeAmount(toRemove)

			contents.amount += (toRemove - notRemoved)
		}

//		println("contents: ${multiblock}")
	}

	companion object {
		private const val MAX_REMOVE_AMOUNT_PER_TICK = 100.0
	}

	fun addToMultiblocks() {
		var deposits = 0

		if (contents.isEmpty()) return

		fun depositToEntities(input: BlockKey) {
			val holders = manager.transportManager.getInputProvider().getHolders(InputType.FLUID, input).filterIsInstance<FluidStoringMultiblock>()
			for (storageEntity in holders) {
				if (!storageEntity.canAdd(contents)) continue
				for (store in storageEntity) {
					if (!store.canAdd(contents)) continue

					val toAdd = minOf((store.capacity - store.getContents().amount), MAX_REMOVE_AMOUNT_PER_TICK, contents.amount)

					store.setAmount(store.getContents().amount + toAdd)
					store.setFluidType(contents.type)
					contents.amount -= toAdd

					deposits++
				}
			}
		}

		for (node in getGraphNodes()) {
			depositToEntities(node.location)
		}

		println(deposits)
	}

	override fun tick() {
		discoverNetwork()

		val volume = cachedVolume ?: getVolume()

		if (contents.amount > volume) {
			contents.amount = volume
		}

		addToMultiblocks()
	}

	var alive: Boolean = true

	fun discoverNetwork() {
		val visitQueue = ArrayDeque<BlockKey>()

		visitQueue.addAll(nodeMirror.keys)

		val visited = LongOpenHashSet()

		var tick = 0

		while (visitQueue.isNotEmpty() && tick < 100 && alive) {
			tick++
			val key = visitQueue.removeFirst()

			if (!manager.cachePoint(key)) continue

			visited.add(key)

			debugAudience.highlightBlock(toVec3i(key), 1L)

			for (face in ADJACENT_BLOCK_FACES) {
				val adjacent = getRelative(key, face)

				if (visitQueue.contains(adjacent) || visited.contains(adjacent)) continue

				visitQueue.add(adjacent)
			}
		}
	}
}
