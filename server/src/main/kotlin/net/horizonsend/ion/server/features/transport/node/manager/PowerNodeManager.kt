package net.horizonsend.ion.server.features.transport.node.manager

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.node.TransportNode
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
		val powerCheck = solarPanelNode.tickAndGetPower()
		if (powerCheck == 0) return@submit

		val destinations: ObjectOpenHashSet<PowerInputNode> = getPowerInputs(solarPanelNode)
		runPowerTransfer(solarPanelNode, destinations.toList(), powerCheck)
	}
}

// These methods are outside the class for speed

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

	// Get a parallel array containing the ascending order of resistances
	val sortedIndexes = getSorted(pathResistance)

	val shareFactors: Array<Double?> = Array(numDestinations) { index ->
		val resistance = pathResistance[index] ?: return@Array null
		val fac = (numDestinations - sortedIndexes[index]).toDouble() / (resistance / maximumResistance)
		shareFactorSum += fac

		fac
	}

	var remainingPower = availableTransferPower

	// Just cast once
	val powerSource = (source as? PowerExtractorNode)

	for ((index, destination) in destinations.withIndex()) {
		val shareFactor = shareFactors[index] ?: continue
		val share = shareFactor / shareFactorSum

		val idealSend = (availableTransferPower * share).roundToInt()
		val toSend = minOf(idealSend, getRemainingCapacity(destination))
		val couldNotRemove = powerSource?.drawPower(toSend) ?: 0 // If null, source is a solar panel, and can't be removed from.

		// Following the power distribution
		val realAdd = toSend - couldNotRemove

		// Amount of power that didn't fit
		val remainder = destination.distributePower(realAdd)
		val realTaken = realAdd - remainder

		remainingPower -= realTaken
		completeChain(paths[index], realTaken)

		if (remainder == 0) continue

		// Get the proportion of the amount of power that sent compared to the ideal calculations.
		val usedShare = realTaken.toDouble() / idealSend.toDouble()
		// Use that to get a proportion of the share factor, and remove that from the sum.
		val toRemove = shareFactor * usedShare
		shareFactorSum -= toRemove
	}

	return remainingPower
}

private fun getRemainingCapacity(destination: PowerInputNode): Int {
	return destination.getPoweredEntities().sumOf { it.storage.getRemainingCapacity() }
}

private fun completeChain(path: Array<TransportNode>?, transferred: Int) {
	path?.filterIsInstance(PowerFlowMeter::class.java)?.forEach { it.onCompleteChain(transferred) }
}

// I hate this function but it works
fun getSorted(pathResistance: Array<Double?>): IntArray {
	// Store the shuffled indicies
	val ranks = IntArray(pathResistance.size) { it }
	val tempSorted = pathResistance.clone()

	for (index in ranks.indices) {
		for (j in 0..< ranks.lastIndex) {
			if ((tempSorted[j] ?: Double.MAX_VALUE) > (tempSorted[j + 1] ?: Double.MAX_VALUE)) {
				val temp = tempSorted[j]
				tempSorted[j] = tempSorted[j + 1]
				tempSorted[j + 1] = temp

				val prev = ranks[j]
				ranks[j] = prev + 1
				ranks[j + 1] = prev
			}
		}
	}

	return ranks
}
