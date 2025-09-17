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
import net.horizonsend.ion.common.utils.miscellaneous.roundToTenThousanth
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendText
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidPortMetadata
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.NetworkManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.FluidPort
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.concurrent.withLock
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull
import kotlin.math.roundToInt
import kotlin.random.Random

@Suppress("UnstableApiUsage")
class FluidNetwork(uuid: UUID, override val manager: NetworkManager<FluidNode, TransportNetwork<FluidNode>>) : TransportNetwork<FluidNode>(uuid, manager) {
	override fun createEdge(nodeOne: FluidNode, nodeTwo: FluidNode): GraphEdge = FluidGraphEdge(nodeOne, nodeTwo)

	/**
	 * A map of each node location to the maximum flow achievable at that node
	 **/
	private var flowMap = Long2DoubleOpenHashMap()

	fun getFlow(position: Long) = flowMap.getOrDefault(position, 0.0)

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

	override fun preSave() {
		getGraphNodes().forEach(FluidNode::populateContents)
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

		// Prevent very small amounts that are annoying to deal with by just deleting them
		if (networkContents.amount.roundToHundredth() == 0.0) networkContents.amount = 0.0

		val (inputs, outputs) = trackIO()

		val delta = (now - lastTransferTick) / 1000.0
		lastTransferTick = now

		tickMultiblockOutputs(outputs, delta)

		if (now - lastDisplayTick > DISPLAY_INTERVAL) {
			lastDisplayTick = now

			displayFluid(outputs)
		}

		tickGauges()

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

		localLock.readLock().withLock {
			for (node in getGraphNodes()) {
				if (node !is FluidNode.LeakablePipe) continue

				val edges = getGraph().outEdges(node)

				if (edges.isEmpty()) continue

				if (edges.size >= 2) continue

				leakingLocations.add(node.location)

				val connectedEdge = edges.first()
				val direction = (connectedEdge as FluidGraphEdge).direction.oppositeFace

				val removeAmount = (minOf(flowMap.getOrDefault(node.location, 0.0), node.leakRate, networkContents.amount) * delta)
				if (removeAmount <= 0) continue

				runCatching { type.getValue().playLeakEffects(manager.transportManager.getWorld(), node, direction) }.onFailure { exception -> exception.printStackTrace() }

				networkContents.amount -= removeAmount

				// Handle pollution
				type.getValue().onLeak(manager.transportManager.getWorld(), toVec3i(node.location).getRelative(direction), removeAmount)
			}
		}

		leakingPipes = leakingLocations
	}

	private var leakingPipes = LongOpenHashSet()

	/**
	 * Returns a pair of a location map of inputs, and a location map of outputs
	 **/
	private fun trackIO(): Pair<Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidPortMetadata>>, Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidPortMetadata>>> {
		val inputs = Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidPortMetadata>>()
		val outputs = Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidPortMetadata>>()

		for (node in getGraphNodes()) {
			val ports: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<FluidPortMetadata>> = manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location)

			for (port in ports) {
				val metaData = port.metaData
				if (metaData.inputAllowed) inputs[node.location] = port
				if (metaData.outputAllowed) outputs[node.location] = port
			}
		}

		return inputs to outputs
	}

	private fun tickMultiblockInputs(inputs: Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidPortMetadata>>, delta: Double) {
		inputs.forEach { entry -> addToMultiblocks(entry.key, entry.value, delta) }
	}

	private fun tickMultiblockOutputs(outputs: Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidPortMetadata>>, delta: Double) {
		outputs.forEach { entry -> depositToNetwork(entry.key, entry.value, delta) }
	}

	private fun tickGauges() {
		val pressure = networkContents.getDataOrDefault(FluidPropertyTypeKeys.PRESSURE, (getGraphNodes().firstOrNull() ?: return).getCenter().toLocation(manager.transportManager.getWorld()))
		val formattedPressure = pressure.value.roundToInt().coerceIn(0, 15)

		for (gauge in getGraphNodes().filterIsInstance<FluidNode.PressureGauge>()) gauge.setOutput(formattedPressure)
	}

	private fun depositToNetwork(location: BlockKey, port: IOPort.RegisteredMetaDataInput<FluidPortMetadata>, delta: Double) {
		if (!port.metaData.outputAllowed) return
		val node = getNodeAtLocation(location) as? FluidPort ?: return

		val removalRate = node.removalCapacity

		var remainingRoom = maxOf(0.0, getVolume() - networkContents.amount)
		if (remainingRoom <= 0.0) return

		val storage = port.metaData.connectedStore
		val storageContents = storage.getContents()

		if (storageContents.isEmpty()) return

		if (!networkContents.isEmpty() && storageContents.type != networkContents.type) return

		val toRemove = minOf(
			removalRate * delta,
			(getVolume() - networkContents.amount),
			storage.getContents().amount,
			flowMap.getOrDefault(location, 0.0) * delta // When no flow, still withdraw
		)

		if (toRemove <= 0) return

		if (!storageContents.isEmpty()) networkContents.type = storageContents.type

		// Make a copy as the amount to be added, then combine with properties into the network
		val combined = storageContents.asAmount(toRemove)

		val notRemoved = storage.removeAmount(toRemove)
		combined.amount -= notRemoved

		val combinationLocation = Location(manager.transportManager.getWorld(), getX(location).toDouble(), getY(location).toDouble(), getZ(location).toDouble())
		networkContents.combine(combined, combinationLocation)
	}

	private fun addToMultiblocks(location: BlockKey, ioPort: IOPort.RegisteredMetaDataInput<FluidPortMetadata>, delta: Double) {
		if (networkContents.isEmpty()) return
		if (!ioPort.metaData.inputAllowed) return

		val node = getNodeAtLocation(location) as? FluidPort ?: return

		val additionRate = node.additionCapacity

		val store = ioPort.metaData.connectedStore

		if (!store.canAdd(networkContents)) return

		if (!store.getContents().isEmpty() && store.getContents().type != networkContents.type) return

		val room = store.capacity - store.getContents().amount
		val availableToMove = networkContents.amount
		val flowLimit = flowMap.getOrDefault(location, 0.0) * delta
		val additionLimit = additionRate * delta
		val toAdd = minOf(room, availableToMove, flowLimit, additionLimit)

		if (toAdd <= 0) return

		val toCombine = networkContents.asAmount(toAdd)
		store.addFluid(toCombine, Location(manager.transportManager.getWorld(), getX(location).toDouble(), getY(location).toDouble(), getZ(location).toDouble()))

		networkContents.amount -= toAdd
	}

	fun displayFluid(outputs: Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidPortMetadata>>) {
		val contents = networkContents

		if (contents.isEmpty()) {
			return
		}

		val type = contents.type

		Tasks.async {
			val world = manager.transportManager.getWorld()

			for (node in getGraphNodes()) {
				debugAudience.sendText(node.getCenter().toLocation(manager.transportManager.getWorld()).add(0.0, 0.5, 0.0), Component.text(flowMap.getOrDefault(node.location, 0.0)), 20L)

				if (node.location in outputs.keys) continue

				val edge = getGraph().outEdges(node).maxByOrNull { edge -> (edge as FluidGraphEdge).netFlow } as? FluidGraphEdge ?: continue

				var childDirection = edge.direction

				if (getFlow(node.location) <= 0 || edge.netFlow == 0.0) {
					childDirection = BlockFace.SELF
				}

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

					type.getValue().displayInPipe(world, origin, destination)
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

		// Grab a node to use as a location for default params
		val node = getGraphNodes().firstOrNull() ?: other.getGraphNodes().firstOrNull()
		val location = node?.getCenter()?.toLocation(manager.transportManager.getWorld())

		// Merge amounts if same type
		otherContents.combine(networkContents, location)
	}

	override fun onSplit(children: Collection<TransportNetwork<FluidNode>>) {
		val contents = networkContents.clone()
		val availableAmount = contents.amount

		val volume = getVolume()

		for (child in children) {
			val share = (child as FluidNetwork).getVolume() / volume
			val due = availableAmount * share
			val toMerge = contents.asAmount(due)
			child.networkContents.combine(toMerge, null)

			contents.amount -= due.roundToTenThousanth() // Prevent float math weirdness
			networkContents.amount -= due.roundToTenThousanth() // Prevent float math weirdness
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
		// Map of nodes to all nodes that connect to them
		val parentRelationMap = Long2LongOpenHashMap()

		val sources = ObjectOpenHashSet<FluidNode>()
		val sinks = ObjectOpenHashSet<FluidNode>()

		getGraphNodes().forEach { node ->
			if (manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location).any { input ->
				val container = input.metaData.connectedStore

				// If the port can have input, has a fluid that can be combined with the network, and has room for more fluid, add to sinks.
				input.metaData.inputAllowed && (container.getContents().canCombine(networkContents) || container.getContents().isEmpty() || networkContents.isEmpty()) && container.getRemainingRoom() > 0.0
			}) sinks.add(node)

			if (manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location).any { input ->
				val container = input.metaData.connectedStore

				// If the port can output, has a fluid that can be combined, and is not empty, add to sources.
				input.metaData.outputAllowed && (networkContents.canCombine(container.getContents()) || networkContents.isEmpty()) && container.getContents().amount > 0.0
			}) sources.add(node)
		}

		if ((sinks.isEmpty() && leakingPipes.isEmpty())) return

		if (sources.isEmpty()) {
			for (node in sinks) {
				flowMap[node.location] = node.flowCapacity
			}
			for (node in leakingPipes.iterator()) {
				flowMap[node] = 1.0
			}
			return
		}

		val valueGraph = getValueGraphRepresentation()

		// Connect all sources to a super source, with a maximum capcity between
		for (source in sources) {
			valueGraph.putEdgeValue(SUPER_SOURCE, source.location, Double.MAX_VALUE)
		}

		// Connect all sinks to a super sink, with a maximum capcity between
		for (sink in sinks) {
			valueGraph.putEdgeValue(sink.location, SUPER_SINK, Double.MAX_VALUE)
		}

		// Treat leaking pipes as sinks, and connect them to the super sink
		for (leaking in leakingPipes.iterator()) {
			valueGraph.putEdgeValue(leaking, SUPER_SINK, 1.0)
		}

		var maxFlow = 0.0

		val endpointFlows = Long2DoubleOpenHashMap()

		var iterations = 0
		while (bfs(valueGraph, parentRelationMap))	{
			iterations++

			if (iterations > 20) {
				break
			}

			var pathFlow = Double.MAX_VALUE
			var node: BlockKey = SUPER_SINK

			while (node != SUPER_SOURCE) {
				var parentOfNode: Long = parentRelationMap.getOrDefault(node, null) ?: break

				pathFlow = minOf(pathFlow, valueGraph.edgeValue(parentOfNode, node).get())
				node = parentOfNode
			}

			maxFlow += pathFlow

			// Loop over nodes and decrement the flow values from the previous loop
			var v: BlockKey = SUPER_SINK
			while (v != SUPER_SOURCE) {
				val parentOfNode: Long = parentRelationMap.getOrDefault(v, null) ?: break

				if (v == parentOfNode) break

				valueGraph.putEdgeValue(parentOfNode, v, valueGraph.edgeValue(parentOfNode, v).get() - pathFlow)
				valueGraph.putEdgeValue(v, parentOfNode, valueGraph.edgeValue(v, parentOfNode).getOrDefault(0.0) + pathFlow)

				v = parentOfNode
			}

			v = SUPER_SINK

			while (v != SUPER_SOURCE) {
				endpointFlows[v] = maxOf(endpointFlows.getOrDefault(v, 0.0), maxFlow)

				val parentOfNode: Long = parentRelationMap.getOrDefault(v, null) ?: break

				if (v == parentOfNode) break

				val parentNode = getNodeAtLocation(parentOfNode)
				val node = getNodeAtLocation(v)

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

	fun getValueGraphRepresentation(): MutableValueGraph<BlockKey, Double> {
		val copied = ValueGraphBuilder
			.directed()
			.allowsSelfLoops(false)
			.expectedNodeCount(getGraphNodes().size)
			.build<BlockKey, Double>()

		for (node in getGraphNodes()) {
			copied.addNode(node.location)
		}

		for (edge in getGraphEdges()) {
			var capacity = (edge.nodeOne as FluidNode).flowCapacity
			if (leakingPipes.contains(edge.nodeOne.location)) {
				capacity = 1.0
			}

			copied.putEdgeValue(edge.nodeOne.location, edge.nodeTwo.location, capacity)
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

	override fun toString(): String {
		return "FluidNetwork{id=$uuid,size=${getGraphNodes().size},contents=$networkContents}"
	}
}
