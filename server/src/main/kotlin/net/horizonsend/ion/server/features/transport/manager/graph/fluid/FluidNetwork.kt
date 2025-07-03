package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.NetworkManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.Trail
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.random.Random

class FluidNetwork(uuid: UUID, override val manager: NetworkManager<FluidNode, TransportNetwork<FluidNode>>) : TransportNetwork<FluidNode>(uuid, manager) {
	override fun createEdge(
		nodeOne: FluidNode,
		nodeTwo: FluidNode,
	): GraphEdge {
		return object : GraphEdge {
			override val nodeOne: TransportNode = nodeOne
			override val nodeTwo: TransportNode = nodeTwo
		}
	}

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

	override fun handleTick() {
		discoverNetwork()

		val volume = getVolume()

		if (networkContents.amount > volume) {
			networkContents.amount = volume
		}

		val (inputs, outputs) = trackIO()

		tickMultiblockOutputs(outputs)

		displayFluid()

		tickMultiblockInputs(inputs)
	}

	fun trackIO(): Pair<ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>, ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>> {
		val inputs = ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>()
		val outputs = ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>()

		for (node in getGraphNodes()) {
			val ports: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<FluidInputMetadata>> = manager.transportManager.getInputProvider().getPorts(IOType.FLUID, node.location)

			for (port in ports) {
				val metaData = port.metaData
				if (metaData.inputAllowed) inputs.add(port)
				if (metaData.outputAllowed) outputs.add(port)
			}
		}

		return inputs to outputs
	}

	fun tickMultiblockInputs(inputs: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>) {
		inputs.forEach(::addToMultiblocks)
	}

	fun tickMultiblockOutputs(outputs: ObjectOpenHashSet<IOPort.RegisteredMetaDataInput<FluidInputMetadata>>) {
		outputs.forEach(::depositToNetwork)
	}

	fun depositToNetwork(input: IOPort.RegisteredMetaDataInput<FluidInputMetadata>) {
		if (!input.metaData.outputAllowed) return

		var remainingRoom = maxOf(0.0, getVolume() - networkContents.amount)
		if (remainingRoom <= 0.0) return

		val storage = input.metaData.connectedStore
		val storageContents = storage.getContents()

		if (storageContents.isEmpty()) return

		if (!networkContents.isEmpty() && storageContents.type != networkContents.type) return

		val toRemove = minOf((getVolume() - networkContents.amount), storage.getContents().amount, 5.0)
		val notRemoved = storage.removeAmount(toRemove)

		networkContents.amount += (toRemove - notRemoved)
		if (!storageContents.isEmpty()) networkContents.type = storageContents.type
	}

	companion object {
		private const val MAX_REMOVE_AMOUNT_PER_TICK = 100.0
	}

	fun addToMultiblocks(ioPort: IOPort.RegisteredMetaDataInput<FluidInputMetadata>) {
		if (networkContents.isEmpty()) return
		if (!ioPort.metaData.inputAllowed) return

		val store = ioPort.metaData.connectedStore

		if (!store.canAdd(networkContents)) return

		if (!store.getContents().isEmpty() && store.getContents().type != networkContents.type) return

		val toAdd = minOf((store.capacity - store.getContents().amount), MAX_REMOVE_AMOUNT_PER_TICK, networkContents.amount)

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
			visitSet.remove(key)

			visited.add(key)

			debugAudience.highlightBlock(toVec3i(key), 3L)

			var toBreak = false

			for (face in ADJACENT_BLOCK_FACES) {
				val adjacent = getRelative(key, face)

				if (nodeMirror.containsKey(adjacent)) continue
				if (visitSet.contains(adjacent) || visited.contains(adjacent)) continue

				val discoveryResult = manager.discoverPosition(adjacent, this)

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

	fun displayFluid() {
		var count = 0

		runnable {
			if (count == 10) cancel()
			count++

			if (networkContents.isEmpty()) {
				return@runnable
			}

			val edges = getGraphEdges().flatMap { it.getDisplayPoints() }

			val world = manager.transportManager.getWorld()

			edges.forEach { vec ->
				vec.add(Vector(
					Random.nextDouble(-0.215, 0.215),
					Random.nextDouble(-0.215, 0.215),
					Random.nextDouble(-0.215, 0.215)
				))

				val trial = Trail(
					vec.toLocation(world).add(0.0, 0.0, 1.0),
					(networkContents.type as? GasFluid)?.color ?: Color.BLUE,
					10
				)

				world.spawnParticle(Particle.TRAIL, vec.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0, trial, false)
			}
		}.runTaskTimer(IonServer, 0L, 2L)
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
}
