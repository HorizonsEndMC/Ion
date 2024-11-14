package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.util.getIdealPath
import net.horizonsend.ion.server.features.transport.util.getNetworkDestinations
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.END_ROD
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.NOTE_BLOCK
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.SPONGE
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Observer
import org.bukkit.craftbukkit.v1_20_R3.block.impl.CraftEndRod
import kotlin.math.roundToInt

class PowerTransportCache(holder: CacheHolder<PowerTransportCache>) : TransportCache(holder) {
	override val type: CacheType = CacheType.POWER
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.addSimpleNode(CRAFTING_TABLE, PowerNode.PowerExtractorNode)
		.addSimpleNode(SPONGE, PowerNode.SpongeNode)
		.addDataHandler<CraftEndRod>(END_ROD) { data, _ -> PowerNode.EndRodNode(data.facing.axis) }
		.addSimpleNode(REDSTONE_BLOCK, PowerNode.PowerMergeNode)
		.addSimpleNode(IRON_BLOCK, PowerNode.PowerMergeNode)
		.addSimpleNode(LAPIS_BLOCK, PowerNode.PowerInvertedMergeNode)
		.addDataHandler<Observer>(OBSERVER) { data, loc -> PowerFlowMeter(this, data.facing, loc) }
		.addSimpleNode(NOTE_BLOCK, PowerInputNode)
		.build()

	override fun tickExtractor(location: BlockKey, delta: Double) { NewTransport.executor.submit {
		val world = holder.getWorld()
		val sources = getPowerExtractorSourcePool(location, world)
		val source = sources.randomOrNull() ?: return@submit //TODO take from all

		// Flood fill on the network to find power inputs, and check input data for multiblocks using that input that can store any power
		val destinations: List<BlockKey> = getNetworkDestinations<PowerInputNode>(CacheType.POWER, world, location) { node ->
			world.ion.inputManager.getHolders(type, node.position).any { entity -> entity is PoweredMultiblockEntity && !entity.powerStorage.isFull() }
		}

		if (destinations.isEmpty()) return@submit

		val transferLimit = (IonServer.transportSettings.extractorConfiguration.maxPowerRemovedPerExtractorTick * delta).roundToInt()
		val transferred = minOf(source.powerStorage.getPower(), transferLimit)

		// Store this just in case
		val missing = source.powerStorage.removePower(transferred)

		val remainder = runPowerTransfer(
			Node.NodePositionData(
				PowerNode.PowerExtractorNode,
				world,
				location,
				BlockFace.SELF
			),
			destinations,
			(transferred - missing)
		)

		if (remainder > 0) {
			source.powerStorage.addPower(remainder)
		}
	}}

	private fun getPowerExtractorSourcePool(extractorLocation: BlockKey, world: World): List<PoweredMultiblockEntity> {
		val sources = mutableListOf<PoweredMultiblockEntity>()

		for (face in ADJACENT_BLOCK_FACES) {
			val inputLocation = getRelative(extractorLocation, face)
			if (holder.getOrCacheGlobalNode(inputLocation) !is PowerInputNode) continue
			val entities = getInputEntities(world, inputLocation)

			for (entity in entities) {
				if (entity !is PoweredMultiblockEntity) continue
				if (entity.powerStorage.isEmpty()) continue
				sources.add(entity)
			}
		}

		return sources
	}

	/**
	 * Runs the power transfer from the source to the destinations. pending rewrite
	 **/
	private fun runPowerTransfer(source: Node.NodePositionData, rawDestinations: List<BlockKey>, availableTransferPower: Int): Int {
		if (rawDestinations.isEmpty()) return availableTransferPower

		val filteredDestinations = rawDestinations.filter { destinationLoc ->
			getInputEntities(holder.getWorld(), destinationLoc).any { it is PoweredMultiblockEntity && !it.powerStorage.isFull() }
		}

		val numDestinations = filteredDestinations.size

		val paths: Array<Array<Node.NodePositionData>?> = Array(numDestinations) { runCatching {
			getIdealPath(source, filteredDestinations[it])
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

		for ((index, destination) in filteredDestinations.withIndex()) {
			val shareFactor = shareFactors[index] ?: continue
			val inputData = PowerInputNode.getPoweredEntities(source.world, destination)

			val share = shareFactor / shareFactorSum

			val idealSend = (availableTransferPower * share).roundToInt()
			val toSend = minOf(idealSend, getRemainingCapacity(inputData))

			// Amount of power that didn't fit
			val remainder = distributePower(inputData, toSend)
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
}

/**
 * Gets the powered entities accessible from this location, assuming it is an input
 * This method is used in conjunction with input registration to allow direct access via signs, and remote access via registered inputs
 **/
fun getInputEntities(world: World, location: BlockKey): Set<MultiblockEntity> {
	val inputManager = world.ion.inputManager
	val registered = inputManager.getHolders(CacheType.POWER, location)

	val adjacentBlocks = stupidOffsets.mapNotNull { MultiblockEntities.getMultiblockEntity(world, it.x, it.y, it.z) }

	return registered.plus(adjacentBlocks)
}

/**
 * Distributes power to the provided list of entities
 * Returns the amount that would not fit
 **/
fun distributePower(destinations: List<PoweredMultiblockEntity>, power: Int): Int {
	val entities = destinations.filterTo(mutableListOf()) { !it.powerStorage.isFull() }
	if (entities.isEmpty()) return power

	// Skip math for most scenarios
	if (entities.size == 1) return entities.first().powerStorage.addPower(power)

	var remainingPower = power

	while (remainingPower > 0) {
		if (entities.isEmpty()) break

		val share = remainingPower / entities.size
		val minRemaining = entities.minOf { it.powerStorage.getRemainingCapacity() }
		val distributed = minOf(minRemaining, share)

		val iterator = entities.iterator()
		while (iterator.hasNext()) {
			val entity = iterator.next()

			val r = entity.powerStorage.addPower(distributed)
			if (entity.powerStorage.isFull()) iterator.remove()

			remainingPower -= (distributed - r)
		}
	}

	return remainingPower
}

private fun getRemainingCapacity(destinations: List<PoweredMultiblockEntity>): Int {
	return destinations.sumOf { it.powerStorage.getRemainingCapacity() }
}

private fun completeChain(path: Array<Node.NodePositionData>?, transferred: Int) {
	path?.forEach { if (it.type is PowerFlowMeter) it.type.onCompleteChain(transferred) }
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

val stupidOffsets: Array<Vec3i> = arrayOf(
	// Upper ring
	Vec3i(1, 1, 0),
	Vec3i(-1, 1, 0),
	Vec3i(0, 1, 1),
	Vec3i(0, 1, -1),
	// Lower ring
	Vec3i(1, -1, 0),
	Vec3i(-1, -1, 0),
	Vec3i(0, -1, 1),
	Vec3i(0, -1, -1),

	// Middle ring
	Vec3i(2, 0, 0),
	Vec3i(-2, 0, 0),
	Vec3i(0, 0, -2),
	Vec3i(0, 0, -2),

	Vec3i(1, 0, 1),
	Vec3i(-1, 0, 1),
	Vec3i(1, 0, -1),
	Vec3i(-1, 0, -1),
)
