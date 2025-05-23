package net.horizonsend.ion.server.features.transport.nodes.cache

//
//class FluidTransportCache(holder: CacheHolder<FluidTransportCache>): TransportCache(holder) {
//	override val type: CacheType = CacheType.FLUID
//	override val extractorNodeClass: KClass<out Node> = FluidNode.FluidExtractorNode::class
//
//	override fun tickExtractor(location: BlockKey, delta: Double, metaData: ExtractorMetaData?) {}
//
//	/**
//	 * Executes the transfer from the source node to the lit of destinations. Transports one fluid at a time.
//	 **/
//	private fun runFluidTransfer(source: Node.NodePositionData, rawDestinations: Collection<BlockKey>, fluid: Fluid, amount: Int): Int {
//		if (rawDestinations.isEmpty()) return amount
//
//		val filteredDestinations = rawDestinations.filter { destinationLoc ->
//			getInputEntities(destinationLoc).any { it is FluidStoringEntity && it.anyCapacityCanStore(fluid) }
//		}
//
//		if (filteredDestinations.isEmpty()) return amount
//
//		val numDestinations = filteredDestinations.size
//
////		val paths: Array<PathfindingReport?> = Array(numDestinations) {
////			findPath(source, filteredDestinations[it]) { node, _ ->
////				(node is FluidNode) && (@Suppress("UNCHECKED_CAST") (node as? FilterNode<Fluid>)?.canTransfer(fluid) != false)
////			}
////		}
////
////		var maximumResistance: Double = -1.0
////
////		// Perform the calc & max find in the same loop
////		val pathResistance: Array<Double?> = Array(numDestinations) {
////			val res = paths[it]?.resistance
////			if (res != null && maximumResistance < res) maximumResistance = res
////
////			res
////		}
////
////		// All null, no paths found
////		if (maximumResistance == -1.0) return amount
////
////		var shareFactorSum = 0.0
////
////		// Get a parallel array containing the ascending order of resistances
////		val sortedIndexes = getSorted(pathResistance)
////
////		val shareFactors: Array<Double?> = Array(numDestinations) { index ->
////			val resistance = pathResistance[index] ?: return@Array null
////			val fac = (numDestinations - sortedIndexes[index]).toDouble() / (resistance / maximumResistance)
////			shareFactorSum += fac
////
////			fac
////		}
////
////		var remainingAmount = amount
////
////		for ((index, destination) in filteredDestinations.withIndex()) {
////			val shareFactor = shareFactors[index] ?: continue
////			val inputData = FluidNode.FluidInputNode.getFluidEntities(source.world, destination)
////
////			val share = shareFactor / shareFactorSum
////
////			val idealSend = (amount * share).roundToInt()
////			val capacity = getRemainingCapacity(fluid, inputData)
////			val toSend = minOf(idealSend, capacity)
////
////			// Amount of power that didn't fit
////			val remainder = distributeFluid(inputData, fluid, toSend)
////			val realTaken = toSend - remainder
////
////			remainingAmount -= realTaken
////			completeChain(paths[index]?.traversedNodes, realTaken)
////
////			if (remainder == 0) continue
////
////			// Get the proportion of the amount of power that sent compared to the ideal calculations.
////			val usedShare = realTaken.toDouble() / idealSend.toDouble()
////			// Use that to get a proportion of the share factor, and remove that from the sum.
////			val toRemove = shareFactor * usedShare
////			shareFactorSum -= toRemove
////		}
//
//		return amount
//	}
//
//	private fun getRemainingCapacity(fluid: Fluid, destinations: List<FluidStoringEntity>): Int {
//		return destinations.sumOf { it.getCapacityFor(fluid) }
//	}
//
//	private fun distributeFluid(destinations: List<FluidStoringEntity>, fluid: Fluid, amount: Int): Int {
//		val entities = destinations.filterTo(mutableListOf()) { it.getCapacityFor(fluid) > 0 }
//		if (entities.isEmpty()) return amount
//
//		// Skip math for most scenarios
//		if (entities.size == 1) return entities.first().addFirstAvailable(FluidStack(fluid, amount))
//
//		var remainingPower = amount
//
//		while (remainingPower > 0) {
//			if (entities.isEmpty()) break
//
//			val share = remainingPower / entities.size
//			val minRemaining = entities.minOf { it.getCapacityFor(fluid) }
//			val distributed = minOf(minRemaining, share)
//
//			val iterator = entities.iterator()
//			while (iterator.hasNext()) {
//				val entity = iterator.next()
//
//				val r = entity.addFirstAvailable(FluidStack(fluid, amount))
//				if (r > 0) iterator.remove()
//
//				remainingPower -= (distributed - r)
//			}
//		}
//
//		return remainingPower
//	}
//
//	private fun completeChain(path: Array<Node.NodePositionData>?, transferred: Int) {
//		path?.forEach { if (it.type is PowerNode.PowerFlowMeter) it.type.onCompleteChain(transferred) }
//	}
//}
