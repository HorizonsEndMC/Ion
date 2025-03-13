package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.configuration.ConfigurationFiles.transportSettings
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.util.PathCache
import net.horizonsend.ion.server.features.transport.nodes.pathfinding.PathfindingNodeWrapper
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
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
		val destinations: Collection<PathfindingNodeWrapper> = getOrCacheDestination<PowerInputNode>(location, originNode) { node ->
			getInputEntities(node.position).any { entity ->
				(entity is PoweredMultiblockEntity) && !entity.powerStorage.isFull()
			}
		}

		if (destinations.isEmpty()) return@runTask

		val transferLimit = (transportSettings().powerConfiguration.maxPowerRemovedPerExtractorTick * delta).roundToInt()

		runPowerTransfer(
			destinations.take(transportSettings().powerConfiguration.maxExtractorDestinations),
			transferLimit,
			source.powerStorage
		)
	}

	private fun tickSolarPanel(location: BlockKey, delta: Double, solarCache: SolarPanelCache) = NewTransport.runTask {
		val transportPower = solarCache.getPower(holder.getWorld(), location, delta)
		if (transportPower == 0) return@runTask

		val cacheResult = holder.nodeCacherGetter.invoke(this, type, holder.getWorld(), location) ?: return@runTask
		val originNode = cacheResult.second ?: return@runTask

		// Flood fill on the network to find power inputs, and check input data for multiblocks using that input that can store any power
		val destinations: Collection<PathfindingNodeWrapper> = getOrCacheDestination<PowerInputNode>(location, originNode) { node ->
			getInputEntities(node.position).any { entity ->
				(entity is PoweredMultiblockEntity) && !entity.powerStorage.isFull()
			}
		}

		if (destinations.isEmpty()) return@runTask

		holder.transportManager.powerNodeManager.cache.runPowerTransfer(
			destinations.take(transportSettings().powerConfiguration.maxSolarDestinations),
			transportPower,
			null
		)
	}

	/**
	 * Runs the power transfer from the source to the destinations. pending rewrite
	 **/
	private fun runPowerTransfer(rawDestinations: List<PathfindingNodeWrapper>, transferLimit: Int, powerStorage: PowerStorage?) {
		if (rawDestinations.isEmpty()) return

		val filteredDestinations = rawDestinations.filter { destinationLoc ->
			getInputEntities(destinationLoc.node.position).any { it is PoweredMultiblockEntity && !it.powerStorage.isFull() }
		}

		if (filteredDestinations.isEmpty()) return

		val numDestinations = filteredDestinations.size
		// Should the power storage be null, it is a solar panel, and the transfer limit was just what is generated
		val removeAmount = minOf((numDestinations * transferLimit), (powerStorage?.getPower() ?: transferLimit))
		// Remove all at the start
		powerStorage?.removePower(removeAmount)
		val individualAmount = removeAmount / numDestinations

		var remainingPower = removeAmount

		for (destination in filteredDestinations) {
			val inputData = getInputEntities(destination.node.position).filterIsInstance<PoweredMultiblockEntity>()

			val remainingCapacity = inputData.sumOf { it.powerStorage.getRemainingCapacity() }
			val toSend = minOf(individualAmount, remainingCapacity)

			// Amount of power that didn't fit
			val remainder = distributePower(inputData, toSend)
			val realTaken = toSend - remainder

			remainingPower -= realTaken

			runCatching {
				// Update power flow meters
				destination.buildPath().forEach { if (it.type is PowerNode.PowerFlowMeter) it.type.onCompleteChain(realTaken) }
			}
		}

		powerStorage?.addPower(remainingPower)
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
