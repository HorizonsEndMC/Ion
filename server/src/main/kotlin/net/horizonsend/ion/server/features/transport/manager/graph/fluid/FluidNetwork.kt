package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.roundToTenThousanth
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendText
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidPortMetadata
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.inputs.IOPort.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.FlowNode
import net.horizonsend.ion.server.features.transport.manager.graph.FlowTrackingTransportGraph
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
import kotlin.math.roundToInt
import kotlin.random.Random

@Suppress("UnstableApiUsage")
class FluidNetwork(uuid: UUID, override val manager: NetworkManager<FluidNode, TransportNetwork<FluidNode>>) : FlowTrackingTransportGraph<FluidNode, RegisteredMetaDataInput<FluidPortMetadata>>(uuid, manager, IOType.FLUID) {
	override fun createEdge(nodeOne: FluidNode, nodeTwo: FluidNode): GraphEdge = FluidGraphEdge(nodeOne, nodeTwo)

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

			// Discover any structural changes and check integrity of the network
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
//		if (networkContents.isEmpty()) return

		val type = networkContents.type

		val leakingLocations = LongOpenHashSet()

		localLock.readLock().withLock {
			for (node in getGraphNodes()) {
				if (node !is FluidNode.LeakablePipe) continue

				val edges = getGraph().outEdges(node)

				if (edges.isEmpty()) continue

				if (edges.size >= 2) continue

				leakingLocations.add(node.location)

				if (networkContents.isEmpty()) continue

				val connectedEdge = edges.first()
				val direction = (connectedEdge as FluidGraphEdge).direction.oppositeFace

				val removeAmount = (minOf(getFlow(node.location), node.leakRate, networkContents.amount) * delta)
				if (removeAmount <= 0) continue

				runCatching { type.getValue().playLeakEffects(manager.transportManager.getWorld(), node, direction) }.onFailure { exception -> exception.printStackTrace() }

				if (networkContents.amount < 0) return@withLock
				networkContents.amount -= minOf(removeAmount, networkContents.amount)

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
	private fun trackIO(): Pair<Long2ObjectOpenHashMap<RegisteredMetaDataInput<FluidPortMetadata>>, Long2ObjectOpenHashMap<RegisteredMetaDataInput<FluidPortMetadata>>> {
		val inputs = Long2ObjectOpenHashMap<RegisteredMetaDataInput<FluidPortMetadata>>()
		val outputs = Long2ObjectOpenHashMap<RegisteredMetaDataInput<FluidPortMetadata>>()

		for (node in getGraphNodes()) {
			val ports: ObjectOpenHashSet<RegisteredMetaDataInput<FluidPortMetadata>> = manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location)

			for (port in ports) {
				val metaData = port.metaData
				if (metaData.inputAllowed) inputs[node.location] = port
				if (metaData.outputAllowed) outputs[node.location] = port
			}
		}

		return inputs to outputs
	}

	private fun tickMultiblockInputs(inputs: Long2ObjectOpenHashMap<RegisteredMetaDataInput<FluidPortMetadata>>, delta: Double) {
		inputs.forEach { entry -> addToMultiblocks(entry.key, entry.value, delta) }
	}

	private fun tickMultiblockOutputs(outputs: Long2ObjectOpenHashMap<RegisteredMetaDataInput<FluidPortMetadata>>, delta: Double) {
		outputs.forEach { entry -> depositToNetwork(entry.key, entry.value, delta) }
	}

	private fun tickGauges() {
		val pressure = networkContents.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE, (getGraphNodes().firstOrNull() ?: return).getCenter().toLocation(manager.transportManager.getWorld()))
		val formattedTemperature = pressure.value.roundToInt().coerceIn(0, 15)

		for (gauge in getGraphNodes().filterIsInstance<FluidNode.TemperatureGauge>()) gauge.setOutput(formattedTemperature, manager.transportManager.getMultiblockmanager(gauge.getGlobalCoordinate()) ?: continue)
	}

	private fun depositToNetwork(location: BlockKey, port: RegisteredMetaDataInput<FluidPortMetadata>, delta: Double) {
		if (!port.metaData.outputAllowed) return
		val node = getNodeAtLocation(location) as? FluidPort ?: return

		val removalRate = node.removalCapacity

		val remainingRoom = maxOf(0.0, getVolume() - networkContents.amount)
		if (remainingRoom <= 0.0) return

		val storage = port.metaData.connectedStore
		val storageContents = storage.getContents()

		if (storageContents.isEmpty()) return

		if (!networkContents.isEmpty() && storageContents.type != networkContents.type) return

		val toRemove = minOf(
			removalRate * delta,
			(getVolume() - networkContents.amount),
			storage.getContents().amount,
			getFlow(location) * delta
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

	private fun addToMultiblocks(location: BlockKey, ioPort: RegisteredMetaDataInput<FluidPortMetadata>, delta: Double) {
		if (networkContents.isEmpty()) return
		if (!ioPort.metaData.inputAllowed) return

		val node = getNodeAtLocation(location) as? FluidPort ?: return

		val additionRate = node.additionCapacity

		val store = ioPort.metaData.connectedStore

		if (!store.canAdd(networkContents)) return

		if (!store.getContents().isEmpty() && store.getContents().type != networkContents.type) return

		val room = store.capacity - store.getContents().amount
		val availableToMove = networkContents.amount
		val flowLimit = getFlow(location) * delta
		val additionLimit = additionRate * delta
		val toAdd = minOf(room, availableToMove, flowLimit, additionLimit)

		if (toAdd <= 0) return

		val toCombine = networkContents.asAmount(toAdd)
		store.addFluid(toCombine, Location(manager.transportManager.getWorld(), getX(location).toDouble(), getY(location).toDouble(), getZ(location).toDouble()))

		networkContents.amount -= toAdd
	}

	fun displayFluid(outputs: Long2ObjectOpenHashMap<RegisteredMetaDataInput<FluidPortMetadata>>) {
		val contents = networkContents

		if (contents.isEmpty()) {
			return
		}

		val type = contents.type

		Tasks.async {
			val world = manager.transportManager.getWorld()

			for (node in getGraphNodes()) {
				debugAudience.sendText(node.getCenter().toLocation(manager.transportManager.getWorld()).add(0.0, 0.5, 0.0), Component.text(getFlow(node.location)), 20L)

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

	override fun isSink(node: FlowNode, ioData: RegisteredMetaDataInput<FluidPortMetadata>?): Boolean {
		if (leakingPipes.contains(node.location)) {
			return true
		}

		if (ioData == null) return false

		val container = ioData.metaData.connectedStore

		// If the port can have input, has a fluid that can be combined with the network, and has room for more fluid, add to sinks.
		return ioData.metaData.inputAllowed
			&& (container.getContents().canCombine(networkContents) || container.getContents().isEmpty() || networkContents.isEmpty())
			&& container.getRemainingRoom() > 0.0
	}

	override fun isSource(node: FlowNode, ioData: RegisteredMetaDataInput<FluidPortMetadata>): Boolean {
		val container = ioData.metaData.connectedStore

		// If the port can output, has a fluid that can be combined, and is not empty, add to sources.
		return ioData.metaData.outputAllowed
			&& (networkContents.canCombine(container.getContents()) || networkContents.isEmpty())
			&& container.getContents().amount > 0.0
	}

	override fun getFlowCapacity(node: FluidNode): Double {
		if (!leakingPipes.contains(node.location)) return super.getFlowCapacity(node)
		return 5.0
	}

	companion object {
		const val PIPE_INTERIOR_PADDING = 0.215

		private const val STRUCTURE_INTERVAL = 1000L
		private const val DISPLAY_INTERVAL = 250L
	}

	override fun toString(): String {
		return "FluidNetwork{id=$uuid,size=${getGraphNodes().size},contents=$networkContents}"
	}
}
