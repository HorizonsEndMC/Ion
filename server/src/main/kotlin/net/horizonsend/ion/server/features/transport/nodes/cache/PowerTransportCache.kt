package net.horizonsend.ion.server.features.transport.nodes.cache

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.FlowMeterDisplay
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache.PowerNode.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.util.NetworkType
import net.horizonsend.ion.server.features.transport.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.util.getIdealPath
import net.horizonsend.ion.server.features.transport.util.getNetworkDestinations
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.Axis
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
import org.bukkit.block.Sign
import org.bukkit.block.data.type.Observer
import org.bukkit.craftbukkit.v1_20_R3.block.impl.CraftEndRod
import kotlin.math.roundToInt

class PowerTransportCache(holder: NetworkHolder<PowerTransportCache>) : TransportCache(holder) {
	override val type: NetworkType = NetworkType.POWER
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

	sealed interface PowerNode : CachedNode {
		override val networkType: NetworkType get() = NetworkType.POWER

		data object SpongeNode : PowerNode {
			override val pathfindingResistance: Double = 1.0
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = true
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = true
			override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
		}

		data class EndRodNode(var axis: Axis) : PowerNode, ComplexCachedNode {
			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = offset.axis == this.axis
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = offset.axis == this.axis
			override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf(backwards.oppositeFace)
			override fun onTranslate(movement: StarshipMovement) {
				this.axis = movement.displaceFace(this.axis.faces.first).axis
			}
		}

		data object PowerExtractorNode : PowerNode {
			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = false
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = other !is PowerInputNode
			override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
		}

		data object PowerMergeNode : PowerNode {
			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = other is SpongeNode
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = other !is SpongeNode
			override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
		}

		data object PowerInvertedMergeNode : PowerNode {
			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = other is EndRodNode
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = other !is EndRodNode
			override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
		}

		data class PowerFlowMeter(val cache: PowerTransportCache, var face: BlockFace, val location: BlockKey) : PowerNode, ComplexCachedNode {
			val displayHandler = DisplayHandlers.newBlockOverlay(
				cache.holder.getWorld(),
				toVec3i(location),
				face,
				FlowMeterDisplay(this, 0.0, 0.0, 0.0, 0.7f)
			).register()

			private val STORED_AVERAGES = 20
			private val averages = mutableListOf<TransferredPower>()

			fun onCompleteChain(transferred: Int) {
				addTransferred(TransferredPower(transferred, System.currentTimeMillis()))
				displayHandler.update()
			}

			private fun addTransferred(transferredSnapshot: TransferredPower) {
				val currentSize = averages.size

				if (currentSize < STORED_AVERAGES) {
					averages.add(transferredSnapshot)
					return
				}

				// If it is full, shift all averages to the right
				for (index in 18 downTo 0) {
					averages[index + 1] = averages[index]
				}

				averages[0] = transferredSnapshot
			}

			private fun calculateAverage(): Double {
				val sum = averages.sumOf { it.transferred }

				val timeDiff = (System.currentTimeMillis() - averages.minOf { it.time }) / 1000.0

				return sum / timeDiff
			}

			fun formatFlow(): Component {
				var avg = runCatching { calculateAverage().roundToHundredth() }.getOrDefault(0.0)

				// If no averages, or no power has been moved in 5 seconds, go to 0
				if (averages.isEmpty() || System.currentTimeMillis() - averages.maxOf { it.time } > 5000) {
					avg = 0.0
				}

				return ofChildren(FlowMeterDisplay.firstLine, text(avg, GREEN), FlowMeterDisplay.secondLine)
			}

			override val pathfindingResistance: Double = 0.5
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = true
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = true
			override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)

			override fun onTranslate(movement: StarshipMovement) {
				this.face = movement.displaceFace(this.face)
				displayHandler.displace(movement)
			}

			private data class TransferredPower(val transferred: Int, val time: Long)
		}

		data object PowerInputNode : PowerNode {
			override val pathfindingResistance: Double = 0.0
			override fun canTransferFrom(other: CachedNode, offset: BlockFace): Boolean = true
			override fun canTransferTo(other: CachedNode, offset: BlockFace): Boolean = false
			override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
			fun getPoweredEntities(world: World, location: BlockKey) = world.ion.inputManager.getHolders(NetworkType.POWER, location).filterIsInstance<PoweredMultiblockEntity>()
		}
	}

	override fun tickExtractor(location: BlockKey, delta: Double) { NewTransport.executor.submit {
		val world = holder.getWorld()
		val sources = getExtractorSourcePool(location, world).filterNot { it.powerStorage.isEmpty() }
		val source = sources.randomOrNull() ?: return@submit //TODO take from all

		val destinations: LongOpenHashSet = getNetworkDestinations<PowerInputNode>(NetworkType.POWER, world, location) {
			world.ion.inputManager.getHolders(type, it.position).isNotEmpty()
		}

		if (destinations.isEmpty()) return@submit

		val transferLimit = (IonServer.transportSettings.extractorConfiguration.maxPowerRemovedPerExtractorTick * delta).roundToInt()
		val transferred = minOf(source.powerStorage.getPower(), transferLimit)
		val notRemoved = source.powerStorage.removePower(transferred)

		val remainder = runPowerTransfer(
			CachedNode.NodePositionData(
				PowerNode.PowerExtractorNode,
				world,
				location,
				BlockFace.SELF
			),
			destinations.toMutableList(),
			(transferred - notRemoved)
		)

//		if (transferred == remainder) {
//			TODO skip growing number of ticks if nothing to do
//		}

		if (remainder > 0) {
			source.powerStorage.addPower(remainder)
		}
	}}
}

// These methods are outside the class for speed
fun getExtractorSourcePool(extractorLocation: BlockKey, world: World): List<PoweredMultiblockEntity> {
	return ADJACENT_BLOCK_FACES.flatMap {
		getPoweredEntities(world, getRelative(extractorLocation, it))
	}.filterIsInstance<PoweredMultiblockEntity>()
}

/**
 * Gets the powered entities accessible from this location, assuming it is an input
 **/
fun getPoweredEntities(world: World, location: BlockKey): Set<MultiblockEntity> {
	val inputManager = world.ion.inputManager
	val registered = inputManager.getHolders(NetworkType.POWER, location)
	val adjacentBlocks = ADJACENT_BLOCK_FACES.mapNotNull {
		val block = getBlockIfLoaded(world, getX(location), getY(location), getZ(location)) ?: return@mapNotNull null
		val adjacent = block.getRelativeIfLoaded(it)?.state as? Sign ?: return@mapNotNull null
		MultiblockEntities.getMultiblockEntity(adjacent)
	}

	return registered.plus(adjacentBlocks)
}

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

/**
 * Runs the power transfer from the source to the destinations. pending rewrite
 **/
private fun runPowerTransfer(source: CachedNode.NodePositionData, destinations: List<BlockKey>, availableTransferPower: Int): Int {
	if (destinations.isEmpty()) return availableTransferPower

	val numDestinations = destinations.size

	val paths: Array<Array<CachedNode.NodePositionData>?> = Array(numDestinations) { runCatching {
		getIdealPath(source, destinations[it])
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

private fun getRemainingCapacity(destinations: List<PoweredMultiblockEntity>): Int {
	return destinations.sumOf { it.powerStorage.getRemainingCapacity() }
}

private fun completeChain(path: Array<CachedNode.NodePositionData>?, transferred: Int) {
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
