package net.horizonsend.ion.server.features.transport.node.manager.node

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
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
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

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
		val availablePower = extractorNode.getTransferPower()
		if (availablePower == 0) return

		val destinations = getNetworkDestinations(extractorNode)
		val share = availablePower.toDouble() / destinations.size.toDouble()

		for (destination in destinations) {
			destination.storage.addPower(share.roundToInt())
		}
	}

	fun tickSolarPanel(panelNode: SolarPanelNode) {
		val powerCheck = panelNode.getPower()
		if (powerCheck == 0) return

		val destinations = getNetworkDestinations(panelNode)
		val realPower = panelNode.tickAndGetPower()
		val share = realPower.toDouble() / destinations.size.toDouble()

		println("Sending $realPower to ${destinations.size} destinations")

		for (destination in destinations) {
			destination.storage.addPower(share.roundToInt())
		}
	}

	private fun getNetworkDestinations(origin: TransportNode): ObjectOpenHashSet<PoweredMultiblockEntity> {
		val visitQueue = ArrayDeque<TransportNode>()
		val visitedSet = ObjectOpenHashSet<TransportNode>()
		val destinations = ObjectOpenHashSet<PoweredMultiblockEntity>()

		visitQueue.addAll(origin.getTransferableNodes())

		while (visitQueue.isNotEmpty()) {
			val currentNode = visitQueue.removeFirst()
			visitedSet.add(currentNode)

			if (currentNode is PowerInputNode && currentNode.isCalling()) {
				destinations.add(currentNode.boundMultiblockEntity)
			}

			visitQueue.addAll(currentNode.cachedTransferable.filterNot { visitedSet.contains(it) })
		}

		return destinations
	}

	override fun finalizeNodes() {
		@Suppress("UNCHECKED_CAST")
		val chunk = (holder as? ChunkNetworkHolder<PowerNodeManager>)?.manager?.chunk ?: return
		chunk.multiblockManager.getAllMultiblockEntities().values.filterIsInstance<PoweredMultiblockEntity>().forEach(::tryBindPowerNode)
	}
}
