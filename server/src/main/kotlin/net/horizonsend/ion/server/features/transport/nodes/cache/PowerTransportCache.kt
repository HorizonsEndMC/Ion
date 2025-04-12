package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.configuration.ConfigurationFiles.transportSettings
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.nodes.util.MonoDestinationCache
import net.horizonsend.ion.server.features.transport.nodes.util.PathfindingNodeWrapper
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import kotlin.math.roundToInt
import kotlin.reflect.KClass

class PowerTransportCache(holder: CacheHolder<PowerTransportCache>) : TransportCache(holder), DestinationCacheHolder {
	override val type: CacheType = CacheType.POWER
	override val extractorNodeClass: KClass<out Node> = PowerNode.PowerExtractorNode::class
	override val destinationCache = MonoDestinationCache(this)

	override fun tickExtractor(location: BlockKey, delta: Double, metaData: ExtractorMetaData?) {
		val solarCache = holder.transportManager.solarPanelManager.cache

		if (solarCache.isSolarPanel(location)) {
			NewTransport.runTask {
				tickSolarPanel(location, delta, solarCache)
			}
		}

		NewTransport.runTask {
			tickPowerExtractor(location, delta)
		}
	}

	private fun tickPowerExtractor(location: BlockKey, delta: Double) {
		val sources = getExtractorSourceEntities<PoweredMultiblockEntity>(location) { it.powerStorage.isEmpty() }
		val source = sources.randomOrNull() ?: return //TODO take from all

		runPowerTransfer(
			rawDestinations = getTransferDestinations(location) ?: return,
			transferLimit = (transportSettings().powerConfiguration.powerTransferRate * delta).roundToInt(),
			powerStorage = source.powerStorage
		)
	}

	private fun tickSolarPanel(location: BlockKey, delta: Double, solarCache: SolarPanelCache) {
		val transportPower = solarCache.getPower(holder.getWorld(), location, delta)
		if (transportPower == 0) return

		runPowerTransfer(
			rawDestinations = getTransferDestinations(location) ?: return,
			transferLimit = transportPower,
			powerStorage = null
		)
	}

	private fun getTransferDestinations(extractorLocation: BlockKey): Collection<PathfindingNodeWrapper>? {
		// Flood fill on the network to find power inputs, and check input data for multiblocks using that input that can store any power
		val destinations: Collection<PathfindingNodeWrapper> = getOrCacheNetworkDestinations<PowerInputNode>(
			originPos = extractorLocation,
			cachingFunction = { destinations ->
				destinationCache.set(PowerNode.PowerExtractorNode::class, extractorLocation, destinations)
			},
			cacheGetter = {
				destinationCache.get(PowerNode.PowerExtractorNode::class, extractorLocation)
			},
			originNode = holder.getOrCacheGlobalNode(extractorLocation) ?: return null,
			destinationCheck = { node ->
				getInputEntitiesTyped<PoweredMultiblockEntity>(node.position).any { entity -> !entity.powerStorage.isFull() }
			}
		)

		if (destinations.isEmpty()) return null

		return destinations
	}

	/**
	 * Runs the power transfer from the source to the destinations. pending rewrite
	 **/
	private fun runPowerTransfer(rawDestinations: Collection<PathfindingNodeWrapper>, transferLimit: Int, powerStorage: PowerStorage?) {
		val numDestinations = rawDestinations.size

		val removeAmount = minOf(
			a = numDestinations * transferLimit,

			// Should the power storage be null, it is a solar panel, and the transfer limit was just what is generated
			b = powerStorage?.getPower() ?: transferLimit
		)

		// Remove all at the start
		val missing = powerStorage?.removePower(removeAmount) ?: 0
		val availableForTransfer = (removeAmount - missing)

		// Ensure that all get at least 1
		val destinations = rawDestinations.take(availableForTransfer)
		if (destinations.isEmpty()) {
			powerStorage?.addPower(removeAmount - missing)
			return
		}

		val individualAmount = availableForTransfer / destinations.size

		// The amount of power that has not been removed
		var remainingPower = removeAmount - missing

		for (destination in destinations) {
			val inputEntities = getInputEntitiesTyped<PoweredMultiblockEntity>(destination.node.position)

			val remainingCapacity = inputEntities.sumOf { it.powerStorage.getRemainingCapacity() }
			if (remainingCapacity == 0) continue

			val toSend = minOf(individualAmount, remainingCapacity)

			// Amount of power that didn't fit
			val remainder = distributePower(inputEntities, toSend)
			val realTaken = toSend - remainder

			remainingPower -= realTaken

			updateFlowMeters(destination, realTaken)
		}

		powerStorage?.addPower(remainingPower)
	}

	private fun updateFlowMeters(destination: PathfindingNodeWrapper, amountTaken: Int) = runCatching {
		destination.buildPath().forEach {
			if (it.type !is PowerNode.PowerFlowMeter) return@forEach
			it.type.onCompleteChain(amountTaken)
		}
	}.onFailure { e ->
		e.printStackTrace()
	}

	/**
	 * Distributes power to the provided list of entities
	 * Returns the amount that would not fit
	 **/
	private fun distributePower(destinations: Collection<PoweredMultiblockEntity>, power: Int): Int {
		val entities = destinations.filterTo(mutableListOf()) { !it.powerStorage.isFull() }
		if (entities.isEmpty()) return power

		// Skip math for most scenarios
		if (entities.size == 1) return entities.first().powerStorage.addPower(power)

		var remainingPower = power
		var previousLoop = remainingPower

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

			if (remainingPower == previousLoop) break
			previousLoop = remainingPower
		}

		return remainingPower
	}
}
