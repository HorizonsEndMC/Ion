package net.horizonsend.ion.server.features.transport.cache

import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.type.power.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.node.type.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.type.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.features.transport.node.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.node.util.getIdealPath
import net.horizonsend.ion.server.features.transport.node.util.getNetworkDestinations
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Axis
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.END_ROD
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.SPONGE
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Observer
import org.bukkit.craftbukkit.v1_20_R3.block.impl.CraftEndRod
import java.util.concurrent.Future
import kotlin.math.roundToInt

class PowerTransportCache(holder: NetworkHolder<PowerTransportCache>) : TransportCache(holder) {
	override val type: NetworkType = NetworkType.POWER
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.addSimpleNode(CRAFTING_TABLE, PowerNode.PowerExtractorNode)
		.addSimpleNode(SPONGE, PowerNode.SpongeNode)
		.addDataHandler<CraftEndRod>(END_ROD) { PowerNode.EndRodNode(it.facing.axis) }
		.addSimpleNode(REDSTONE_BLOCK, PowerNode.PowerMergeNode)
		.addSimpleNode(IRON_BLOCK, PowerNode.PowerMergeNode)
		.addSimpleNode(LAPIS_BLOCK, PowerNode.PowerInvertedMergeNode)
		.addDataHandler<Observer>(OBSERVER) { PowerNode.PowerFlowMeter(it.facing) }
		.build()

	sealed interface PowerNode : CachedNode {
		fun anyDirection(inputDirection: BlockFace) = ADJACENT_BLOCK_FACES.minus(inputDirection.oppositeFace).map { it to 1 }

		data object SpongeNode : PowerNode {
			override val pathfindingResistance: Double = 1.0
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = true
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = true
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = anyDirection(inputDirection)
		}

		data class EndRodNode(val axis: Axis) : PowerNode {
			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = offset.axis == this.axis
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = false
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = listOf(inputDirection to 1) // Forward only
		}

		data object PowerExtractorNode : PowerNode {
			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = false
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = true
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = anyDirection(inputDirection)
		}

		data object PowerMergeNode : PowerNode {
			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = other is SpongeNode
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = other !is SpongeNode
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = listOf(
				inputDirection to 10,
				*ADJACENT_BLOCK_FACES.minus(inputDirection).minus(inputDirection.oppositeFace).map { it to 1 }.toTypedArray()
			)
		}

		data object PowerInvertedMergeNode : PowerNode {
			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = other is EndRodNode
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = other !is EndRodNode
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = listOf(
				inputDirection to 10,
				*ADJACENT_BLOCK_FACES.minus(inputDirection).minus(inputDirection.oppositeFace).map { it to 1 }.toTypedArray()
			)
		}

		data class PowerFlowMeter(val face: BlockFace) : PowerNode {
			//TODO display

			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = true
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = true
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = listOf(inputDirection to 1) // Forward only
		}

		data class PowerInputNode(val pos: BlockKey) : PowerNode {
			override val pathfindingResistance: Double = 0.0
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = true
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = false
			override fun getNextNodes(inputDirection: BlockFace): Collection<Pair<BlockFace, Int>> = anyDirection(inputDirection)
		}
	}

	fun tickExtractor(extractorNode: PowerNode.PowerExtractorNode): Future<*> = NewTransport.executor.submit {
//		val source = extractorNode.getSourcePool().filterNot { it.powerStorage.isEmpty() }.randomOrNull() ?: return@submit
//
//		val destinations: ObjectOpenHashSet<PowerInputNode> = getPowerInputs(extractorNode)
//		destinations.removeAll(extractorNode.getTransferableNodes().filterIsInstanceTo(ObjectOpenHashSet()))
//
//		if (destinations.isEmpty()) return@submit
//
//		val transferred = minOf(source.powerStorage.getPower(), powerCheck)
//		val notRemoved = source.powerStorage.removePower(transferred)
//		val remainder = runPowerTransfer(extractorNode, destinations.toMutableList(), (transferred - notRemoved))
//
//		if (transferred == remainder) {
//			//TODO skip growing number of ticks if nothing to do
//		}
//
//		if (remainder > 0) {
//			source.powerStorage.addPower(remainder)
//		}
	}

	private fun tickSolarPanel(solarPanelNode: SolarPanelNode) = NewTransport.executor.submit {
		val powerCheck = solarPanelNode.tickAndGetPower()
		if (powerCheck == 0) return@submit

//		val destinations: ObjectOpenHashSet<PowerInputNode> = getPowerInputs(solarPanelNode)
//		runPowerTransfer(solarPanelNode, destinations.toMutableList(), powerCheck)
	}
}

// These methods are outside the class for speed

fun getPowerInputs(world: World, origin: BlockKey) = getNetworkDestinations<PowerTransportCache.PowerNode.PowerInputNode>(world, NetworkType.POWER, origin) { true }

/**
 * Runs the power transfer from the source to the destinations. pending rewrite
 **/
private fun runPowerTransfer(world: World, sourcePos: BlockKey, sourceType: CachedNode, destinations: List<PowerInputNode>, availableTransferPower: Int): Int {
	if (destinations.isEmpty()) return availableTransferPower

	val numDestinations = destinations.size

	val paths: Array<Array<CachedNode>?> = Array(numDestinations) { runCatching {
		getIdealPath(world, NetworkType.POWER, sourceType, sourcePos, destinations[it].position)
	}.getOrNull() }

	var maximumResistance: Double = -1.0

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

	for ((index, destination) in destinations.withIndex()) {
		val shareFactor = shareFactors[index] ?: continue
		val share = shareFactor / shareFactorSum

		val idealSend = (availableTransferPower * share).roundToInt()
		val toSend = minOf(idealSend, getRemainingCapacity(destination))

		// Amount of power that didn't fit
		val remainder = destination.distributePower(toSend)
		val realTaken = toSend - remainder

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
	return destination.getPoweredEntities().sumOf { it.powerStorage.getRemainingCapacity() }
}

private fun completeChain(path: Array<CachedNode>?, transferred: Int) {
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
