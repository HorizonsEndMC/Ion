package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.configuration.ConfigurationFiles.transportSettings
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.util.PathCache
import net.horizonsend.ion.server.features.transport.nodes.pathfinding.PathTracker
import net.horizonsend.ion.server.features.transport.nodes.pathfinding.calculatePathResistance
import net.horizonsend.ion.server.features.transport.nodes.pathfinding.getIdealPath
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.BlockFace
import kotlin.math.roundToInt
import kotlin.reflect.KClass

class PowerTransportCache(holder: CacheHolder<PowerTransportCache>) : TransportCache(holder) {
	override val type: CacheType = CacheType.POWER
	override val extractorNodeClass: KClass<out Node> = PowerNode.PowerExtractorNode::class

	override val pathCache: PathCache<PathfindingReport> = PathCache.standard(this)

	override fun tickExtractor(location: BlockKey, delta: Double, metaData: ExtractorMetaData?) {
		val solarCache = holder.transportManager.solarPanelManager.cache
		if (solarCache.isSolarPanel(location)) tickSolarPanel(location, delta, solarCache)

		tickPowerExtractor(location, delta)
	}

	private fun tickPowerExtractor(location: BlockKey, delta: Double) =	NewTransport.runTask {
		val world = holder.getWorld()

		val sources = getExtractorSourceEntities<PoweredMultiblockEntity>(location) { it.powerStorage.isEmpty() }
		val source = sources.randomOrNull() ?: return@runTask //TODO take from all

		val cacheResult = holder.nodeCacherGetter.invoke(this, type, holder.getWorld(), location) ?: return@runTask
		val originNode = cacheResult.second ?: return@runTask

		// Flood fill on the network to find power inputs, and check input data for multiblocks using that input that can store any power
		val destinations: Collection<BlockKey> = getOrCacheDestination<PowerInputNode>(location, originNode) { node ->
			getInputEntities(node.position).any { entity ->
				(entity is PoweredMultiblockEntity) && !entity.powerStorage.isFull()
			}
		}

		if (destinations.isEmpty()) return@runTask

		val transferLimit = (transportSettings().powerConfiguration.maxPowerRemovedPerExtractorTick * delta).roundToInt()
		val transferred = minOf(source.powerStorage.getPower(), transferLimit)

		// Store this just in case
		val missing = source.powerStorage.removePower(transferred)

		val remainder = runPowerTransfer(
			NodePositionData(
				PowerNode.PowerExtractorNode,
				world,
				location,
				BlockFace.SELF,
				this
			),
			destinations.take(transportSettings().powerConfiguration.maxExtractorDestinations),
			(transferred - missing)
		)


		if (remainder > 0) {
			source.powerStorage.addPower(remainder)
		}
	}

	private fun tickSolarPanel(location: BlockKey, delta: Double, solarCache: SolarPanelCache) = NewTransport.runTask {
		val transportPower = solarCache.getPower(holder.getWorld(), location, delta)
		if (transportPower == 0) return@runTask

		val cacheResult = holder.nodeCacherGetter.invoke(this, type, holder.getWorld(), location) ?: return@runTask
		val originNode = cacheResult.second ?: return@runTask

		// Flood fill on the network to find power inputs, and check input data for multiblocks using that input that can store any power
		val destinations: Collection<BlockKey> = getOrCacheDestination<PowerInputNode>(location, originNode) { node ->
			getInputEntities(node.position).any { entity ->
				(entity is PoweredMultiblockEntity) && !entity.powerStorage.isFull()
			}
		}

		if (destinations.isEmpty()) return@runTask

		holder.transportManager.powerNodeManager.cache.runPowerTransfer(
			NodePositionData(
				PowerNode.PowerExtractorNode,
				holder.getWorld(),
				location,
				BlockFace.SELF,
				this
			),
			destinations.take(transportSettings().powerConfiguration.maxSolarDestinations),
			transportPower
		)
	}

	/**
	 * Runs the power transfer from the source to the destinations. pending rewrite
	 **/
	fun runPowerTransfer(source: NodePositionData, rawDestinations: List<BlockKey>, availableTransferPower: Int): Int {
		if (rawDestinations.isEmpty()) return availableTransferPower

		val filteredDestinations = rawDestinations.filter { destinationLoc ->
			getInputEntities(destinationLoc).any { it is PoweredMultiblockEntity && !it.powerStorage.isFull() }
		}

		if (filteredDestinations.isEmpty()) return availableTransferPower

		val numDestinations = filteredDestinations.size

		var maximumResistance: Double = -1.0
		var minimumResistance = 0.0

		val pathTracker = PathTracker()

		// Perform the calc & max find in the same loop
		val paths: Array<PathfindingReport?> = Array(numDestinations) {
			val path = findPath(source, pathTracker, filteredDestinations[it])

			if (path != null && maximumResistance < path.resistance) maximumResistance = path.resistance
			if (path != null && minimumResistance > path.resistance) minimumResistance = path.resistance

			path
		}

		var totalShares = 0.0

		val pathShares: Array<Double?> = Array(numDestinations) {
			val res = paths[it]?.resistance ?: return@Array null

			val share = -res * (1 / (maximumResistance - minimumResistance)) + 1.25
			totalShares += share

			share
		}

		if (totalShares == 0.0) return availableTransferPower

		var remainingPower = availableTransferPower

		for ((index, destination) in filteredDestinations.withIndex()) {
			val shareFactor = pathShares[index] ?: continue
			val inputData = getInputEntities(destination).filterIsInstance<PoweredMultiblockEntity>()

			val share = shareFactor / totalShares

			val idealSend = (availableTransferPower * share).roundToInt()
			val remainingCapacity = inputData.sumOf { it.powerStorage.getRemainingCapacity() }

			val toSend = minOf(idealSend, remainingCapacity)

			// Amount of power that didn't fit
			val remainder = distributePower(inputData, toSend)
			val realTaken = toSend - remainder

			remainingPower -= realTaken

			runCatching {
				// Update power flow meters
				paths[index]?.traversedNodes?.forEach { if (it.type is PowerFlowMeter) it.type.onCompleteChain(realTaken) }
			}
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

	fun findPath(origin: NodePositionData, pathTracker: PathTracker, destination: BlockKey, pathfindingFilter: ((Node, BlockFace) -> Boolean)? = null): PathfindingReport? {
		val nodeBlockPositionData = NodePositionData(
			type = PowerInputNode,
			world = origin.world,
			position = destination,
			offset = BlockFace.SELF,
			cache = this
		)

		return pathCache.getOrCompute(origin.position, destination) {
			val path = runCatching {
				getIdealPath(
					from = nodeBlockPositionData,
					destination = origin.position,
					pathTracker = pathTracker,
					cachedNodeProvider = holder.nodeCacherGetter,
					pathfindingFilter = pathfindingFilter
				)
			}.getOrNull()

			if (path == null) return@getOrCompute null

			val resistance = calculatePathResistance(path)
			PathfindingReport(path, resistance)
		}
	}
}
