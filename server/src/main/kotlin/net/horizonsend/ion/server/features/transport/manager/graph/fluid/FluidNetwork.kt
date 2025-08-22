package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraph
import com.google.common.graph.ValueGraphBuilder
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.NetworkManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

@Suppress("UnstableApiUsage")
class FluidNetwork(uuid: UUID, override val manager: NetworkManager<FluidNode, TransportNetwork<FluidNode>>) : TransportNetwork<FluidNode>(uuid, manager) {
	override fun createEdge(nodeOne: FluidNode, nodeTwo: FluidNode): GraphEdge = FluidGraphEdge(nodeOne, nodeTwo)

	var isAlive: Boolean = true; private set

	/**
	 * A map of each node location to the maximum flow achievable at that node
	 **/
	private var flowMap = Long2DoubleOpenHashMap()

	/**
	 * The contents of the network, contained as a fluid stack
	 **/
	var networkContents: FluidStack = FluidStack.empty()

	/**
	 * The last calculated volume of the network
	 **/
	private var cachedVolume: Double? = null

	fun resetCachedVolume() {
		cachedVolume = null
	}

	@Synchronized
	fun getVolume(): Double {
		if (cachedVolume != null) return cachedVolume!!

		val new = getGraphNodes().sumOf { it.volume }
		cachedVolume = new
		return new
	}

	override fun onModified() {
		resetCachedVolume()
	}

	private var lastStructureTick: Long = System.currentTimeMillis()
	private var lastDisplayTick: Long = System.currentTimeMillis()
	private var lastTransferTick: Long = System.currentTimeMillis()

	override fun handleTick() {
		val now = System.currentTimeMillis()

		if (now - lastStructureTick > STRUCTURE_INTERVAL) {
			lastStructureTick = now

			// Discover any strucural changes and check integrity of the network
			discoverNetwork()

			// Determine the direction and capacity for flow through the network
			edmondsKarp()
		}

		val volume = getVolume()

		if (networkContents.amount > volume) {
			networkContents.amount = volume
		}

		if (networkContents.amount.roundToHundredth() == 0.0) networkContents.amount = 0.0

		val (inputs, outputs) = trackIO()

		val delta = (now - lastTransferTick) / 1000.0
		lastTransferTick = now

		tickMultiblockOutputs(outputs, delta)

		if (now - lastDisplayTick > DISPLAY_INTERVAL) {
			lastDisplayTick = now

			displayFluid(outputs)
		}

		tickUnpairedPipes(delta)

		tickMultiblockInputs(inputs, delta)
	}

	/**
	 * Unpaired pipes will leak fluids out of the network
	 **/
	private fun tickUnpairedPipes(delta: Double) {
		if (networkContents.isEmpty()) return

		val type = networkContents.type

		val leakingLocations = LongOpenHashSet()

		for (node in getGraphNodes()) {
			if (node !is FluidNode.LeakablePipe) continue

			val edges = getGraph().outEdges(node)

			if (edges.isEmpty()) continue

			if (edges.size >= 2) continue

			leakingLocations.add(node.location)

			val connectedEdge = edges.first()
			val direction = (connectedEdge as FluidGraphEdge).direction.oppositeFace

			runCatching { type.playLeakEffects(manager.transportManager.getWorld(), node, direction) }.onFailure { exception -> exception.printStackTrace() }

			val removeAmount = (minOf(flowMap.getOrDefault(node.location, 5.0), node.leakRate, networkContents.amount) * delta)
			networkContents.amount -= removeAmount

			// Handle pollution
			type.onLeak(manager.transportManager.getWorld(), toVec3i(node.location).getRelative(direction), removeAmount)
		}

		leakingPipes = leakingLocations
	}

	private var leakingPipes = LongOpenHashSet()

	/**
	 * Returns a pair of a location map of inputs, and a location map of outputs
	 **/
	private fun trackIO(): Pair<Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>, Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>> {
		val inputs = Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>()
		val outputs = Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>()

		for (node in getGraphNodes()) {
			val ports: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<FluidInputMetadata>> = manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location)

			for (port in ports) {
				val metaData = port.metaData
				if (metaData.inputAllowed) inputs[node.location] = port
				if (metaData.outputAllowed) outputs[node.location] = port
			}
		}

		return inputs to outputs
	}

	private fun tickMultiblockInputs(inputs: Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>, delta: Double) {
		inputs.forEach { entry -> addToMultiblocks(entry.key, entry.value, delta) }
	}

	private fun tickMultiblockOutputs(outputs: Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>, delta: Double) {
		outputs.forEach { entry -> depositToNetwork(entry.key, entry.value, delta) }
	}

	private fun depositToNetwork(location: BlockKey, input: IOPort.RegisteredMetaDataInput<FluidInputMetadata>, delta: Double) {
		if (!input.metaData.outputAllowed) return

		var remainingRoom = maxOf(0.0, getVolume() - networkContents.amount)
		if (remainingRoom <= 0.0) return

		val storage = input.metaData.connectedStore
		val storageContents = storage.getContents()

		if (storageContents.isEmpty()) return

		if (!networkContents.isEmpty() && storageContents.type != networkContents.type) return

		val toRemove = minOf((getVolume() - networkContents.amount), storage.getContents().amount, flowMap.getOrDefault(location, 5.0) * delta)
		val notRemoved = storage.removeAmount(toRemove)

		networkContents.amount += (toRemove - notRemoved)
		if (!storageContents.isEmpty()) networkContents.type = storageContents.type
	}

	private fun addToMultiblocks(location: BlockKey, ioPort: IOPort.RegisteredMetaDataInput<FluidInputMetadata>, delta: Double) {
		if (networkContents.isEmpty()) return
		if (!ioPort.metaData.inputAllowed) return

		val store = ioPort.metaData.connectedStore

		if (!store.canAdd(networkContents)) return

		if (!store.getContents().isEmpty() && store.getContents().type != networkContents.type) return

		val toAdd = minOf((store.capacity - store.getContents().amount), networkContents.amount, flowMap.getOrDefault(location, 5.0) * delta)

		store.setAmount(store.getContents().amount + toAdd)
		store.setFluidType(networkContents.type)
		networkContents.amount -= toAdd
	}

	private fun discoverNetwork() {
		val visitQueue = ArrayDeque<BlockKey>()
		// A set is maintained to allow faster checks of
		val visitSet = LongOpenHashSet()

		visitQueue.addAll(nodeMirror.keys)
		visitSet.addAll(nodeMirror.keys)

		val visited = LongOpenHashSet()

		var tick = 0

		while (visitQueue.isNotEmpty() && tick < 10000 && isAlive) whileLoop@{
			tick++
			val key = visitQueue.removeFirst()
			val node = nodeMirror[key] ?: continue
			visitSet.remove(key)

			visited.add(key)

			var toBreak = false

			for (face in node.getPipableDirections()) {
				val adjacent = getRelative(key, face)

				if (nodeMirror.containsKey(adjacent)) continue
				if (visitSet.contains(adjacent) || visited.contains(adjacent)) continue

				val discoveryResult = manager.discoverPosition(adjacent, face, this)

				// Check the node here
				if (discoveryResult is NetworkManager.NodeRegistrationResult.Nothing) continue
				if (discoveryResult is NetworkManager.NodeRegistrationResult.CombinedGraphs) {
					toBreak = true
					break
				}

				visitQueue.add(adjacent)
			}

			if (toBreak) {
				break
			}
		}
	}

	fun displayFluid(outputs: Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>) {
		val contents = networkContents

		if (contents.isEmpty()) {
			return
		}

		val type = contents.type

		Tasks.async {
			val world = manager.transportManager.getWorld()

			for (node in getGraphNodes()) {
				if (node.location in outputs.keys) continue

				val edge = getGraph().outEdges(node).maxByOrNull { edge -> (edge as FluidGraphEdge).netFlow } as? FluidGraphEdge ?: continue
				var childDirection = edge.direction
				if (edge.netFlow == 0.0) childDirection = BlockFace.SELF

				// Flow from parent
				val parent = edge.nodeOne as FluidNode

				edge.getDisplayPoints().forEach { origin ->
					origin.add(Vector(
						Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING),
						Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING),
						Random.nextDouble(-PIPE_INTERIOR_PADDING, PIPE_INTERIOR_PADDING)
					))

					val destination =
						if (childDirection == BlockFace.SELF) origin
						else Vector(
							(origin.x + childDirection.direction.x).coerceIn(getX(parent.location) + PIPE_INTERIOR_PADDING..getX(parent.location) + 1.0 - PIPE_INTERIOR_PADDING),
							(origin.y + childDirection.direction.y).coerceIn(getY(parent.location) + PIPE_INTERIOR_PADDING..getY(parent.location) + 1.0 - PIPE_INTERIOR_PADDING),
							(origin.z + childDirection.direction.z).coerceIn(getZ(parent.location) + PIPE_INTERIOR_PADDING..getZ(parent.location) + 1.0 - PIPE_INTERIOR_PADDING),
						)

					type.displayInPipe(world, origin, destination)
				}
			}
		}
	}

	override fun save(adapterContext: PersistentDataAdapterContext): PersistentDataContainer {
		val pdc = adapterContext.newPersistentDataContainer()

		return pdc
	}

	override fun onMergedInto(other: TransportNetwork<FluidNode>) {
		if (networkContents.isEmpty()) return

		other as FluidNetwork

		val otherContents = other.networkContents
		if (!otherContents.isEmpty() && otherContents.type != networkContents.type) return

		// Merge amounts if same type
		otherContents.amount += networkContents.amount
		otherContents.type = networkContents.type
	}

	override fun onSplit(children: Collection<TransportNetwork<FluidNode>>) {
		val availableAmount = networkContents.amount
		val contents = networkContents.clone()

		// associate with share of remaining
		val remainingChildRoom = children.associateWithNotNull { child: TransportNetwork<FluidNode> ->
			if (child !is FluidNetwork) return@associateWithNotNull null

			val childContents = child.networkContents
			if (!childContents.isEmpty() && networkContents.type != childContents.type) return@associateWithNotNull null

			resetCachedVolume()

			val childVolume = child.getVolume()

			if (childVolume <= 0.0) {
				return@associateWithNotNull 0.0
			}

			availableAmount / (child.getVolume() - childContents.amount)
		}

		if (remainingChildRoom.isEmpty()) return

		var remaining = availableAmount

		for ((child, share) in remainingChildRoom) {
			child as FluidNetwork
			val childDue = remaining * share

			val splitContents = contents.asAmount(childDue)

			child.networkContents = splitContents
		}
	}

	companion object {
		const val PIPE_INTERIOR_PADDING = 0.215

		private const val STRUCTURE_INTERVAL = 1000L
		private const val DISPLAY_INTERVAL = 250L

		private const val SUPER_SOURCE = Long.MAX_VALUE
		private const val SUPER_SINK = Long.MIN_VALUE
	}

	/**
	 * Runs a multi node and multi sink implementation of the Edmonds-Karp algorithm to determine flow direction and magnitude throughout the network
	 **/
	fun edmondsKarp() {
		// Multimap of nodes to all nodes that connect to them
		val parentRelationMap = Long2LongOpenHashMap()

		val sources = getGraphNodes().filterTo(ObjectOpenHashSet()) { node -> manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location).any { input -> input.metaData.outputAllowed } }
		val sinks: ObjectOpenHashSet<FluidNode> = getGraphNodes().filterTo(ObjectOpenHashSet()) { node -> manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location).any { input -> input.metaData.inputAllowed } }

		if (sinks.isEmpty() || sources.isEmpty()) return

		val valueGraph = getValueGraphRepresentation()

		for (source in sources) {
			valueGraph.putEdgeValue(SUPER_SOURCE, source.location, Double.MAX_VALUE)
		}

		for (sink in sinks) {
			valueGraph.putEdgeValue(sink.location, SUPER_SINK, Double.MAX_VALUE)
		}

		// Treat leaking pipes as sinks
		for (leaking in leakingPipes.iterator()) {
			valueGraph.putEdgeValue(leaking, SUPER_SINK, Double.MAX_VALUE)
		}

		var maxFlow = 0.0

		val endpointFlows = Long2DoubleOpenHashMap()

		var iterations = 0
		while (bfs(valueGraph, parentRelationMap))	{
			iterations++

			if (iterations > 7) break

			var pathFlow = Double.MAX_VALUE

			var node: BlockKey = SUPER_SINK

			var iterations = 0L

			while (node != SUPER_SOURCE) {
				iterations++
				var parentOfNode: Long = parentRelationMap.getOrDefault(node, null) ?: break

				pathFlow = minOf(pathFlow, valueGraph.edgeValue(parentOfNode, node).get())
				node = parentOfNode
			}

			maxFlow += pathFlow

			var v: BlockKey = SUPER_SINK
			while (v != SUPER_SOURCE) {
				val parentOfNode: Long = parentRelationMap.getOrDefault(v, null) ?: break

				if (v == parentOfNode) break

				val u = parentOfNode
				valueGraph.putEdgeValue(u, v, valueGraph.edgeValue(u, v).get() - pathFlow)
				valueGraph.putEdgeValue(v, u, valueGraph.edgeValue(v, u).getOrDefault(0.0) + pathFlow)

				v = parentOfNode
			}

			v = SUPER_SINK

			while (v != SUPER_SOURCE) {
				endpointFlows[v] = maxOf(endpointFlows.getOrDefault(v, 0.0), maxFlow)

				val parentOfNode: Long = parentRelationMap.getOrDefault(v, null) ?: break

				if (v == parentOfNode) break

				val parentNode = nodeMirror[parentOfNode]
				val node = nodeMirror[v]

				if (parentNode != null && node != null) {
					val edgeConnecting = getGraph().edgeConnecting(parentNode, node).getOrNull()

					edgeConnecting?.let { edge ->
						edge as FluidGraphEdge

						val newFlow = maxOf(edge.netFlow, maxFlow)
						edge.netFlow = newFlow
					}
				}

				v = parentOfNode
			}
		}

		flowMap = endpointFlows
	}

	fun getValueGraphRepresentation(resetFlow: Boolean = true): MutableValueGraph<BlockKey, Double> {
		val copied = ValueGraphBuilder
			.directed()
			.allowsSelfLoops(false)
			.expectedNodeCount(getGraphNodes().size)
			.build<BlockKey, Double>()

		for (node in getGraphNodes()) {
			copied.addNode(node.location)
		}

		for (edge in getGraphEdges()) {
			if (resetFlow) (edge as FluidGraphEdge).netFlow = 0.0
			copied.putEdgeValue(edge.nodeOne.location, edge.nodeTwo.location, (edge.nodeOne as FluidNode).flowCapacity)
		}

		return copied
	}

	private fun bfs(valueGraphReprestation: ValueGraph<BlockKey, Double>, parents: Long2LongOpenHashMap): Boolean {
		val visited = LongOpenHashSet()
		val queue = ArrayDeque<BlockKey>()

		queue.add(SUPER_SOURCE)
		visited.add(SUPER_SOURCE)

		var iterations = 0L

		while (queue.isNotEmpty()) {
			val parent = queue.removeFirstOrNull() ?: break

			iterations++

			for (successor in valueGraphReprestation.successors(parent)) {
				if (visited.contains(successor)) continue

				val capacity = valueGraphReprestation.edgeValue(parent, successor).getOrNull() ?: continue
				if (capacity <= 0.0) continue

				visited.add(successor)
				queue.addLast(successor)

				parents[successor] = (parent)
			}
		}

		return visited.contains(SUPER_SINK)
	}
}
