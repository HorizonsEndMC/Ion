package net.horizonsend.ion.server.features.transport.node.manager.node

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.node.NetworkType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.holders.ChunkNetworkHolder
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.power.PowerNodeFactory
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.POWER_TRANSPORT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.NamespacedKey
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
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
		runBlocking {
			val block = getBlockIfLoaded(world, x, y, z)
			if (block != null) createNodeFromBlock(block)
		}

		val attemptTwo = getNode(inputKey) as? PowerInputNode ?: return

		new.bindInputNode(attemptTwo)
	}

	fun tick() {

	}

	fun tickExtractor(extractorNode: PowerExtractorNode): Set<PowerInputNode> {
		val visitQueue: Queue<TransportNode> = LinkedList()
		val visitedSet = ObjectOpenHashSet<TransportNode>()
		val destinations = mutableSetOf<PowerInputNode>()

		visitQueue.addAll(extractorNode.getTransferableNodes())

		var transferrable = 0L

		val iterationTime = measureNanoTime {
			while (visitQueue.isNotEmpty()) {
				val currentNode = visitQueue.poll()
				visitedSet.add(currentNode)

				if (currentNode is PowerInputNode) {
					destinations.add(currentNode)
				}

				transferrable += measureNanoTime {
					visitQueue.addAll(currentNode.getTransferableNodes().filterNot { visitedSet.contains(it) || visitQueue.contains(it) })
				}
			}
		}

		println("getting next nodes time: $transferrable")
		println("iterationTime: $iterationTime")

		return destinations
	}

	override fun finalizeNodes() {
		@Suppress("UNCHECKED_CAST")
		val chunk = (holder as? ChunkNetworkHolder<PowerNodeManager>)?.manager?.chunk ?: return
		chunk.multiblockManager.getAllMultiblockEntities().values.filterIsInstance<PoweredMultiblockEntity>().forEach(::tryBindPowerNode)
	}
}
