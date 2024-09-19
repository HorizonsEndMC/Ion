package net.horizonsend.ion.server.features.transport.node.manager

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.holders.ChunkNetworkHolder
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.type.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.node.type.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerNodeFactory
import net.horizonsend.ion.server.features.transport.node.type.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.features.transport.node.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.node.util.getIdealPath
import net.horizonsend.ion.server.features.transport.node.util.getNetworkDestinations
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

	fun tickTransport() {
		extractors.values.forEach(::tickExtractor)
		solarPanels.forEach(::tickSolarPanel)
	}

	private fun tickExtractor(extractorNode: PowerExtractorNode) = NewTransport.executor.submit {
		val powerCheck = extractorNode.getTransferPower()
		if (powerCheck == 0) return@submit

		val destinations: ObjectOpenHashSet<PowerInputNode> = getPowerInputs(extractorNode)
		val transferred = extractorNode.getTransferPower()
		runPowerTransfer(extractorNode, destinations.toList(), transferred)
		extractorNode.markTicked()
	}

	private fun tickSolarPanel(solarPanelNode: SolarPanelNode) = NewTransport.executor.submit {
		val powerCheck = solarPanelNode.getPower()
		if (powerCheck == 0) return@submit

		val destinations: ObjectOpenHashSet<PowerInputNode> = getPowerInputs(solarPanelNode)
		runPowerTransfer(solarPanelNode, destinations.toList(), solarPanelNode.tickAndGetPower())
	}

	private fun getPowerInputs(origin: TransportNode) = getNetworkDestinations<PowerInputNode>(origin) { it.isCalling() }

	/**
	 * Runs the power transfer from the source to the destinations. pending rewrite
	 **/
	private fun runPowerTransfer(source: TransportNode, destinations: List<PowerInputNode>, availableTransferPower: Int): Int {
		if (destinations.isEmpty()) return availableTransferPower
		val numDestinations = destinations.size

		var maximumResistance: Double = -1.0

		val paths: Array<Array<TransportNode>?> = Array(numDestinations) { runCatching {
			getIdealPath(source, destinations[it])
		}.getOrNull() }

		// Perform the calc & max find in the same loop
		val pathResistance: Array<Double?> = Array(numDestinations) {
			val res = calculatePathResistance(paths[it])
			if (res != null && maximumResistance < res) maximumResistance = res

			res
		}

		// All null, no paths found
		if (maximumResistance == -1.0) return availableTransferPower

		var shareFactorSum = 0.0

		val shareFactors: Array<Double?> = Array(numDestinations) { index ->
			val resistance = pathResistance[index] ?: return@Array null
			val fac = (numDestinations - index).toDouble() / (resistance / maximumResistance)
			shareFactorSum += fac

			fac
		}

		var remainingPower = availableTransferPower

		// Just cast once
		val powerSource = source as? PowerExtractorNode

		for ((index, destination) in destinations.withIndex()) {
			val shareFactor = shareFactors[index] ?: continue
			val share = shareFactor / shareFactorSum

			val recipient = destination.boundMultiblockEntity

			if (recipient == null) {
				// Remove this share factor from the sum, since it is not taking up any power
				shareFactorSum -= shareFactor
				continue
			}

			val idealSend = (availableTransferPower * share).roundToInt()
			val toSend = minOf(idealSend, recipient.storage.getRemainingCapacity())
			val couldNotRemove = powerSource?.drawPower(toSend) ?: 0 // If null, source is a solar panel, and can't be removed from.

			val realAdd = toSend - couldNotRemove

			val remainder = recipient.storage.addPower(realAdd)
			val realTaken = realAdd - remainder

			remainingPower -= realTaken
			paths[index]?.filterIsInstance(PowerFlowMeter::class.java)?.forEach { it.onCompleteChain(realTaken) }

			// All sent
			if (remainder == 0) continue

			// Get the proportion of the amount of power that sent compared to the ideal calculations.
			val usedShare = realTaken.toDouble() / idealSend.toDouble()
			// Use that to get a proportion of the share factor, and remove that from the sum.
			val toRemove = shareFactor * usedShare
			shareFactorSum -= toRemove
		}

		return remainingPower
	}

	override fun finalizeNodes() {
		@Suppress("UNCHECKED_CAST")
		val chunk = (holder as? ChunkNetworkHolder<PowerNodeManager>)?.manager?.chunk ?: return
		chunk.multiblockManager.getAllMultiblockEntities().values.filterIsInstance<PoweredMultiblockEntity>().forEach(::tryBindPowerNode)
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
}
