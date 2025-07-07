package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraph
import com.google.common.graph.ValueGraphBuilder
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
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
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.Trail
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

	private var flowMap = Long2DoubleOpenHashMap()

	var networkContents: FluidStack = FluidStack.empty()
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

	private var lastStructureTick: Long = System.currentTimeMillis()
	private var lastDisplayTick: Long = System.currentTimeMillis()
	private var lastTransferTick: Long = System.currentTimeMillis()

	override fun handleTick() {
		val now = System.currentTimeMillis()

		if (now - lastStructureTick > STRUCTURE_INTERVAL) {
			lastStructureTick = now

			discoverNetwork()
			edmondsKarp()
		}

		val volume = getVolume()

		if (networkContents.amount > volume) {
			networkContents.amount = volume
		}

		val (inputs, outputs) = trackIO()

		val delta = (now - lastTransferTick) / 1000.0
		lastTransferTick = now

		tickMultiblockOutputs(outputs, delta)

		if (now - lastDisplayTick > DISPLAY_INTERVAL) {
			lastDisplayTick = now

			displayFluid(outputs)
		}

		tickMultiblockInputs(inputs, delta)
	}

	fun trackIO(): Pair<Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>, Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>> {
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

	fun tickMultiblockInputs(inputs: Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>, delta: Double) {
		inputs.forEach { entry -> addToMultiblocks(entry.key, entry.value, delta) }
	}

	fun tickMultiblockOutputs(outputs: Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>, delta: Double) {
		outputs.forEach { entry -> depositToNetwork(entry.key, entry.value, delta) }
	}

	fun depositToNetwork(location: BlockKey, input: IOPort.RegisteredMetaDataInput<FluidInputMetadata>, delta: Double) {
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

	fun addToMultiblocks(location: BlockKey, ioPort: IOPort.RegisteredMetaDataInput<FluidInputMetadata>, delta: Double) {
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

	var alive: Boolean = true

	fun discoverNetwork() {
		val visitQueue = ArrayDeque<BlockKey>()
		// A set is maintained to allow faster checks of
		val visitSet = LongOpenHashSet()

		visitQueue.addAll(nodeMirror.keys)
		visitSet.addAll(nodeMirror.keys)

		val visited = LongOpenHashSet()

		var tick = 0

		while (visitQueue.isNotEmpty() && tick < 10000 && alive) whileLoop@{
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

	fun displayFluid(outputs: Long2ObjectOpenHashMap<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>) = Tasks.async {
		val contents = networkContents

		if (contents.isEmpty()) {
			return@async
		}

		val color = (contents.type as? GasFluid)?.color ?: Color.BLUE

		val world = manager.transportManager.getWorld()

		for (node in getGraphNodes()) {
			if (node.location in outputs.keys) continue

			val edge = getGraph().outEdges(node).maxByOrNull { edge -> (edge as FluidGraphEdge).netFlow } as? FluidGraphEdge ?: continue
			val flowDirection = edge.direction

			val node = edge.nodeTwo as FluidNode

			val points = edge.getDisplayPoints()

			val padding = 0.215

			points.forEach { vec ->
				vec.add(Vector(
					Random.nextDouble(-padding, padding),
					Random.nextDouble(-padding, padding),
					Random.nextDouble(-padding, padding)
				))

				val destination = Vector(
					(vec.x + flowDirection.direction.x).coerceIn(getX(node.location) + padding..getX(node.location) + 1.0 - padding),
					(vec.y + flowDirection.direction.y).coerceIn(getY(node.location) + padding..getY(node.location) + 1.0 - padding),
					(vec.z + flowDirection.direction.z).coerceIn(getZ(node.location) + padding..getZ(node.location) + 1.0 - padding),
				).toLocation(manager.transportManager.getWorld())

				val trial = Trail(
					/* target = */ destination,
					/* color = */ color,
					/* duration = */ 20
				)

				world.spawnParticle(Particle.TRAIL, vec.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0, trial, false)
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

		// associate with share of remaining
		val remainingChildRoom = children.associateWithNotNull { child: TransportNetwork<FluidNode> ->
			if (child !is FluidNetwork) return@associateWithNotNull null

			val childContents = child.networkContents
			if (!childContents.isEmpty() && networkContents.type != childContents.type) return@associateWithNotNull null

			availableAmount / (child.getVolume() - childContents.amount)
		}

		if (remainingChildRoom.isEmpty()) return

		var remaining = availableAmount

		for ((child, share) in remainingChildRoom) {
			child as FluidNetwork
			val childDue = remaining * share

			child.networkContents.amount += childDue
			child.networkContents.type = networkContents.type
		}
	}

	companion object {
		private const val STRUCTURE_INTERVAL = 1000L
		private const val DISPLAY_INTERVAL = 250L

		private const val SUPER_SOURCE = Long.MAX_VALUE
		private const val SUPER_SINK = Long.MIN_VALUE
	}

	fun edmondsKarp() {
		// Multimap of nodes to all nodes that connect to them
		val parentRelationMap = Long2LongOpenHashMap()

		val sources = getGraphNodes().filterTo(ObjectOpenHashSet()) { node -> manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location).any { input -> input.metaData.outputAllowed } }
		val sinks: ObjectOpenHashSet<FluidNode> = getGraphNodes().filterTo(ObjectOpenHashSet()) { node -> manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location).any { input -> input.metaData.inputAllowed } }

		val valueGraph = getValueGraphRepresentation()

		for (source in sources) {
			valueGraph.putEdgeValue(SUPER_SOURCE, source.location, Double.MAX_VALUE)
		}

		for (sink in sinks) {
			valueGraph.putEdgeValue(sink.location, SUPER_SINK, Double.MAX_VALUE)
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
					val edgeConnecting = getGraph().edgeConnecting(node, parentNode).getOrNull()

					edgeConnecting?.let { edge ->
						edge as FluidGraphEdge

						edge.netFlow = maxOf(edge.netFlow, maxFlow)
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
