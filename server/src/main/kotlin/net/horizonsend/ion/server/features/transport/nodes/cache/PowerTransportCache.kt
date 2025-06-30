package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.configuration.ConfigurationFiles.transportSettings
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.TransportTask
import net.horizonsend.ion.server.features.transport.inputs.InputType
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.nodes.util.MonoDestinationCache
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.CombinedSolarPanel
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import kotlin.math.roundToInt
import kotlin.reflect.KClass

class PowerTransportCache(holder: CacheHolder<PowerTransportCache>) : TransportCache(holder), DestinationCacheHolder {
	override val type: CacheType = CacheType.POWER
	override val extractorNodeClass: KClass<out Node> = PowerNode.PowerExtractorNode::class
	override val destinationCache = MonoDestinationCache(this)

	override fun tickExtractor(location: BlockKey, delta: Double, metaData: ExtractorMetaData?, index: Int, count: Int) {
//		val solarCache = holder.transportManager.solarPanelManager.cache
//		val solarInterval = transportSettings().powerConfiguration.solarPanelTickInterval
//
//		val chunkLength = count.toDouble() / solarInterval.toDouble()
//		val offset = holder.transportManager.tickNumber % solarInterval
//
//		val isLastChunk = (holder.transportManager.tickNumber + 1) % solarInterval < offset
//
//		// Capture the remainder if it is the last chunk
//		val solarTickRange = (offset * chunkLength).toInt() ..< if (isLastChunk) Int.MAX_VALUE else ((offset + 1) * chunkLength).toInt()
//
//		if (solarTickRange.contains(index)) {
//			if (solarCache.isSolarPanel(location)) {
//				NewTransport.runTask(location, holder.getWorld()) {
//					tickSolarPanel(this, location, delta, solarCache)
//				}
//			}
//		}

		NewTransport.runTask(location, holder.getWorld()) {
			tickPowerExtractor(this, location, delta)
		}
	}

	private fun tickPowerExtractor(task: TransportTask, location: BlockKey, delta: Double) {
		val sources = getExtractorSourceEntities<PoweredMultiblockEntity>(location) { it.powerStorage.isEmpty() } // Filter not
		val source = sources.randomOrNull() ?: return //TODO take from all

		runPowerTransfer(
			task = task,
			rawDestinations = getTransferDestinations(task, location) ?: return,
			transferLimit = (transportSettings().powerConfiguration.powerTransferRate * delta).roundToInt(),
			powerStorage = source.powerStorage
		)
	}

	private fun tickSolarPanel(task: TransportTask, location: BlockKey, delta: Double, solarCache: SolarPanelCache) {
		val transportPower = solarCache.getPower(location, delta)
		if (solarCache.combinedSolarPanelPositions.containsKey(location)) return

		if (transportPower == 0) return

		runPowerTransfer(
			task = task,
			rawDestinations = getTransferDestinations(task, location) ?: return,
			transferLimit = transportPower,
			powerStorage = null
		)
	}

	fun tickCombinedSolarPanel(panel: CombinedSolarPanel, delta: Double) {
		val startPosition = panel.extractorPositions.random()

		NewTransport.runTask(startPosition, holder.getWorld()) {
			val transportPower = panel.getPower(delta)
			if (transportPower == 0) return@runTask

			runPowerTransfer(
				task = this,
				rawDestinations = getTransferDestinations(this, location) ?: return@runTask,
				transferLimit = transportPower,
				powerStorage = null
			)
		}
	}

	private fun getTransferDestinations(task: TransportTask, extractorLocation: BlockKey): Array<PathfindResult>? {
		// Flood fill on the network to find power inputs, and check input data for multiblocks using that input that can store any power
		val destinations: Array<PathfindResult> = getOrCacheNetworkDestinations<PowerInputNode>(
			task = task,
			originPos = extractorLocation,
			originNode = holder.getOrCacheGlobalNode(extractorLocation) ?: return null,
			retainFullPath = false,
			cachingFunction = { destinations ->
				destinationCache.set(PowerNode.PowerExtractorNode::class, extractorLocation, destinations)
			},
			cacheGetter = {
				destinationCache.get(PowerNode.PowerExtractorNode::class, extractorLocation)
			},
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
	private fun runPowerTransfer(task: TransportTask, rawDestinations: Array<PathfindResult>, transferLimit: Int, powerStorage: PowerStorage?) {
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
			if (task.isInterrupted()) break

			val inputEntities = getInputEntitiesTyped<PoweredMultiblockEntity>(destination.destinationPosition)

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

	private fun updateFlowMeters(destination: PathfindResult, amountTaken: Int) = runCatching {
		destination.trackedPath.forEach {
			val node = it.second
			if (node !is PowerNode.PowerFlowMeter) return@forEach
			node.onCompleteChain(amountTaken)
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

	/**
	 * Gets the powered entities accessible from this location, assuming it is an input
	 * This method is used in conjunction with input registration to allow direct access via signs, and remote access via registered inputs
	 **/
	fun getInputEntities(location: BlockKey): Set<MultiblockEntity> {
		return holder.getInputManager().getInputs(InputType.POWER, location).mapTo(mutableSetOf()) { it.holder }
	}

	/**
	 * Gets the powered entities accessible from this location, assuming it is an input
	 * This method is used in conjunction with input registration to allow direct access via signs, and remote access via registered inputs
	 **/
	inline fun <reified T> getInputEntitiesTyped(location: BlockKey): Set<T> {
		return holder.getInputManager().getInputs(InputType.POWER, location).filterIsInstanceTo(mutableSetOf())
	}

	inline fun <reified T> getExtractorSourceEntities(extractorLocation: BlockKey, filterNot: (T) -> Boolean): List<T> {
		val sources = mutableListOf<T>()

		for (face in ADJACENT_BLOCK_FACES) {
			val inputLocation = getRelative(extractorLocation, face)
			if (holder.getOrCacheGlobalNode(inputLocation) !is PowerNode.PowerInputNode) continue
			val entities = getInputEntities(inputLocation)

			for (entity in entities) {
				if (entity !is T) continue
				if (filterNot.invoke(entity)) continue
				sources.add(entity)
			}
		}

		return sources
	}
}
