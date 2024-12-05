package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.solarpanel.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.util.getIdealPath
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.END_ROD
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.NOTE_BLOCK
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.SPONGE
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

	override fun tickExtractor(location: BlockKey, delta: Double) {
		val solarCache = holder.transportManager.solarPanelManager.cache
		if (solarCache.isSolarPanel(location)) tickSolarPanel(location, delta, solarCache)

		tickPowerExtractor(location, delta)
	}

	private fun tickPowerExtractor(location: BlockKey, delta: Double) = NewTransport.executor.submit {
		val world = holder.getWorld()
		val sources = getExtractorSources<PoweredMultiblockEntity>(location) { it.powerStorage.isEmpty() }
		val source = sources.randomOrNull() ?: return@submit //TODO take from all

		// Flood fill on the network to find power inputs, and check input data for multiblocks using that input that can store any power
		val destinations: List<BlockKey> = getNetworkDestinations<PowerInputNode>(location) { node ->
			world.ion.inputManager.getHolders(type, node.position).any { entity -> entity is PoweredMultiblockEntity && !entity.powerStorage.isFull() }
		}

		if (destinations.isEmpty()) return@submit

		val transferLimit = (IonServer.transportSettings.powerConfiguration.maxPowerRemovedPerExtractorTick * delta).roundToInt()
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
			destinations.take(IonServer.transportSettings.powerConfiguration.maxExtractorDestinations),
			(transferred - missing)
		)

		if (remainder > 0) {
			source.powerStorage.addPower(remainder)
		}
	}

	private fun tickSolarPanel(location: BlockKey, delta: Double, solarCache: SolarPanelCache) = NewTransport.executor.submit {
		val transportPower = solarCache.getPower(holder.getWorld(), location, delta)
		if (transportPower == 0) return@submit

		// Flood fill on the network to find power inputs, and check input data for multiblocks using that input that can store any power
		val destinations: List<BlockKey> = getNetworkDestinations<PowerInputNode>(location) { node ->
			holder.getWorld().ion.inputManager.getHolders(CacheType.POWER, node.position).any { entity -> entity is PoweredMultiblockEntity && !entity.powerStorage.isFull() }
		}

		if (destinations.isEmpty()) return@submit

		holder.transportManager.powerNodeManager.cache.runPowerTransfer(
			Node.NodePositionData(
				PowerNode.PowerExtractorNode,
				holder.getWorld(),
				location,
				BlockFace.SELF
			),
			destinations.take(IonServer.transportSettings.powerConfiguration.maxSolarDestinations),
			transportPower
		)
	}

	/**
	 * Runs the power transfer from the source to the destinations. pending rewrite
	 **/
	fun runPowerTransfer(source: Node.NodePositionData, rawDestinations: List<BlockKey>, availableTransferPower: Int): Int {
		if (rawDestinations.isEmpty()) return availableTransferPower

		val filteredDestinations = rawDestinations.filter { destinationLoc ->
			getInputEntities(destinationLoc).any { it is PoweredMultiblockEntity && !it.powerStorage.isFull() }
		}

		if (filteredDestinations.isEmpty()) return availableTransferPower

		val numDestinations = filteredDestinations.size

		val paths: Array<Array<Node.NodePositionData>?> = Array(numDestinations) { runCatching {
			getIdealPath(source, filteredDestinations[it], holder.nodeProvider)
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
			val remainingCapacity = inputData.sumOf { it.powerStorage.getRemainingCapacity() }

			val toSend = minOf(idealSend, remainingCapacity)

			// Amount of power that didn't fit
			val remainder = distributePower(inputData, toSend)
			val realTaken = toSend - remainder

			remainingPower -= realTaken

			runCatching {
				// Update power flow meters
				paths[index]?.forEach { if (it.type is PowerFlowMeter) it.type.onCompleteChain(realTaken) }
			}

			if (remainder == 0) continue

			// Get the proportion of the amount of power that sent compared to the ideal calculations.
			val usedShare = realTaken.toDouble() / idealSend.toDouble()
			// Use that to get a proportion of the share factor, and remove that from the sum.
			val toRemove = shareFactor * usedShare
			shareFactorSum -= toRemove
		}

		return remainingPower
	}

	/**
	 * Distributes power to the provided list of entities
	 * Returns the amount that would not fit
	 **/
	private fun distributePower(destinations: List<PoweredMultiblockEntity>, power: Int): Int {
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
}
