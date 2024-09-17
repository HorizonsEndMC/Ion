package net.horizonsend.ion.server.features.transport.node.manager.node

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.node.NetworkType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.holders.ChunkNetworkHolder
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.power.EndRodNode
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.power.PowerNodeFactory
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.power.SpongeNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.POWER_TRANSPORT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.NamespacedKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import kotlin.system.measureNanoTime

class PowerNodeManager(holder: NetworkHolder<PowerNodeManager>) : NodeManager(holder) {
	override val type: NetworkType = NetworkType.POWER
	override val namespacedKey: NamespacedKey = POWER_TRANSPORT
	override val nodeFactory: PowerNodeFactory = PowerNodeFactory(this)

	val extractors: ConcurrentHashMap<BlockKey, PowerExtractorNode> = ConcurrentHashMap()

	/** Store solar panels for ticking */
	val solarPanels: ObjectOpenHashSet<SolarPanelNode> = ObjectOpenHashSet()

	override val dataVersion: Int = 0 //TODO 1

	override fun clearData() {
		nodes.clear()
		solarPanels.clear()
		extractors.clear()
	}

	/**
	 * Handle the addition of a new powered multiblock entity
	 **/
	fun tryBindPowerNode(new: PoweredMultiblockEntity) {
		// All directions
		val inputVec = new.getRealInputLocation()
		val inputKey = toBlockKey(inputVec)

		val inputNode = getNode(inputKey) as? PowerInputNode

		if (inputNode != null) {
			new.bindInputNode(inputNode)
			return
		}

		val (x, y, z) = inputVec
		val block = getBlockIfLoaded(world, x, y, z)
		if (block != null) createNodeFromBlock(block)

		val attemptTwo = getNode(inputKey) as? PowerInputNode ?: return

		new.bindInputNode(attemptTwo)
	}

	fun tick() {
		extractors.values.forEach(::tickExtractor)
		solarPanels.forEach(::tickSolarPanel)
	}

	fun tickExtractor(extractorNode: PowerExtractorNode) {
		val powerCheck = extractorNode.getTransferPower()
		if (powerCheck == 0) return
		val destinations: ObjectOpenHashSet<PowerInputNode>

		val floodFillTime: Long
		val transferTime: Long
		val fullTransferTime = measureNanoTime {
			floodFillTime = measureNanoTime { destinations = getNetworkDestinations(extractorNode) }

			extractorNode.markTicked()
			transferTime = measureNanoTime { runPowerTransfer(extractorNode, destinations.toList(), extractorNode.getTransferPower()) }
		}


		println("Solar panel transfer took $fullTransferTime, flood fill took $floodFillTime, pathfind & transfer calcs took $transferTime")
	}

	fun tickSolarPanel(panelNode: SolarPanelNode) {
		val powerCheck = panelNode.getPower()
		if (powerCheck == 0) return
		val destinations: ObjectOpenHashSet<PowerInputNode>

		val floodFillTime: Long
		val transferTime: Long
		val fullTransferTime = measureNanoTime {
			floodFillTime = measureNanoTime { destinations = getNetworkDestinations(panelNode) }

			transferTime = measureNanoTime { runPowerTransfer(panelNode, destinations.toList(), panelNode.tickAndGetPower()) }
		}

		println("Solar panel transfer took $fullTransferTime, flood fill took $floodFillTime, pathfind & transfer calcs took $transferTime")
	}

	private fun getNetworkDestinations(origin: TransportNode): ObjectOpenHashSet<PowerInputNode> {
		val visitQueue = ArrayDeque<TransportNode>()
		val visitedSet = ObjectOpenHashSet<TransportNode>()
		val destinations = ObjectOpenHashSet<PowerInputNode>()

		visitQueue.addAll(origin.getTransferableNodes())

		while (visitQueue.isNotEmpty()) {
			val currentNode = visitQueue.removeFirst()
			visitedSet.add(currentNode)

			if (currentNode is PowerInputNode && currentNode.isCalling()) {
				destinations.add(currentNode)
			}

			visitQueue.addAll(currentNode.cachedTransferable.filterNot { visitedSet.contains(it) })
		}

		return destinations
	}

	/**
	 * Runs the power transfer fr
	 **/
	fun runPowerTransfer(source: TransportNode, destinations: List<PowerInputNode>, availableTransferPower: Int) {
		if (destinations.isEmpty()) return
		println("Sending $availableTransferPower to ${destinations.size} destinations")

		val numDestinations = destinations.size

		var maximumResistance: Double = -1.0

		val paths: Array<List<TransportNode>?> = Array(numDestinations) { runCatching { getPath(source, destinations[it]) }.getOrNull() }

		// Perform the calc & max find in the same loop
		val pathResistance: Array<Double?> = Array(numDestinations) {
			val res = calculatePathResistance(paths[it])
			if (res != null && maximumResistance < res) maximumResistance = res

			res
		}

		// All null, no paths found
		if (maximumResistance == -1.0) return

		var shareSum = 0.0

		val shareFactors: Array<Double?> = Array(numDestinations) { index ->
			val resistance = pathResistance[index] ?: return@Array null
			val fac = (numDestinations - index).toDouble() / (resistance / maximumResistance)
			shareSum += fac

			fac
		}

		for ((index, destination) in destinations.withIndex()) {
			val shareFactor = shareFactors[index] ?: return
			val share = shareFactor / shareSum

			val sent = availableTransferPower * share // TODO more complex transfer code

			paths[index]?.filterIsInstance(PowerFlowMeter::class.java)?.forEach { it.onCompleteChain(sent.roundToInt()) }
			destination.boundMultiblockEntity?.storage?.addPower(sent.toInt())
		}
	}

	fun calculatePathResistance(path: List<TransportNode>?): Double? {
		if (path == null) return null

		return 1.0
	}

	/**
	 * Uses the A* algorithm to find the shortest available path between these two nodes.
	 **/
	private fun getPath(from: TransportNode, to: TransportNode): List<TransportNode>? {
		val queue = ArrayDeque<NodeContainer>(1)
		queue.add(NodeContainer(from, null, 0, getHeuristic(from, to)))

		val visited = mutableListOf<NodeContainer>()

		var iterations = 0

		while (queue.isNotEmpty() && iterations < 150) {
			iterations++
			val current = queue.minBy { it.f }

			if (current.node == to) {
				return current.collectPath()
			}

			queue.remove(current)
			visited.add(current)

			for (neighbor in getNeighbors(current)) {
				if (visited.contains(neighbor)) continue
				neighbor.f = neighbor.g + getHeuristic(neighbor.node, to)

				val existingNeighbor = queue.firstOrNull { it.node === neighbor.node }
				if (existingNeighbor != null) {
					if (neighbor.g < existingNeighbor.g) {
						existingNeighbor.g = neighbor.g
						existingNeighbor.parent = neighbor.parent
					}
				} else {
					queue.add(neighbor)
				}
			}
		}

		return null
	}

	private fun getNeighbors(node: NodeContainer): List<NodeContainer> {
		return node.node.getTransferableNodes().map {
			NodeContainer(
				node = it,
				parent = node,
				g = node.g + 1,
				f = 1
			)
		}
	}

	data class NodeContainer(val node: TransportNode, var parent: NodeContainer?, var g: Int, var f: Int) {
		fun collectPath(): List<TransportNode> {
			val set = mutableSetOf<TransportNode>()
			var current: NodeContainer? = this

			set.add(this.node)

			while (current?.parent != null) {
				current = current.parent!!
				set.add(current.node)
			}

			return set.toList()
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as NodeContainer

			if (node != other.node) return false
			if (g != other.g) return false
			return f == other.f
		}

		override fun hashCode(): Int {
			var result = node.hashCode()
			result = 31 * result + g
			result = 31 * result + f
			return result
		}
	}

	fun getResistance(node: TransportNode): Int = when (node) {
		is SpongeNode -> node.positions.size
		is EndRodNode -> 2
		else -> 1
	}

	fun getHeuristic(node: TransportNode, destination: TransportNode): Int {
		val b = getResistance(node)

		val originPos = toVec3i(((node as? SingleNode)?.position ?: (node as? MultiNode<*, *>)?.positions?.first())!!)
		val destinationPos = toVec3i(((destination as? SingleNode)?.position ?: (destination as? MultiNode<*, *>)?.positions?.first())!!)

		return originPos.distance(destinationPos).roundToInt() + b
	}

	override fun finalizeNodes() {
		@Suppress("UNCHECKED_CAST")
		val chunk = (holder as? ChunkNetworkHolder<PowerNodeManager>)?.manager?.chunk ?: return
		chunk.multiblockManager.getAllMultiblockEntities().values.filterIsInstance<PoweredMultiblockEntity>().forEach(::tryBindPowerNode)
	}
}
