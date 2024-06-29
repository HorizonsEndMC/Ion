package net.horizonsend.ion.server.features.transport.step.origin

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode

class ExtractorPowerOrigin(
	val extractorNode: PowerExtractorNode
) : StepOrigin<ChunkPowerNetwork>, PowerOrigin {
	override val transferLimit: Int = extractorNode.getTransferPower()
	/**
	 * Remove power from the origin extractor's multiblocks.
	 *
	 * @return the amount of power that could not be removed
	 **/
	fun removeOrigin(amount: Int): Int {
		// List of multiblock entities that power may be taken from
		val availableEntities = extractorNode.extractableNodes
			.flatMap { it.getPoweredMultiblocks() }
			.distinct()
			.filterNotTo(mutableListOf()) { (it as MultiblockEntity).removed || it.isEmpty() }

		val minRemove = minOf(amount, availableEntities.minOfOrNull { it.getPower() } ?: return amount)

		println("to remove $amount")

		val share = minRemove / availableEntities.size

		var removeRemaining = minRemove

		// Remove an equal share from each of the inventories
		for (entity in availableEntities) {
			entity as MultiblockEntity
			if (entity.removed) continue

			// Should never be more than 0, but handle the possibility
			val notRemoved = entity.removePower(share)
			println("Didn't remove $notRemoved")
			removeRemaining -= (share - notRemoved)
			println("removeRemaining: $removeRemaining")
		}

		availableEntities.firstOrNull { it.canRemovePower(removeRemaining) }?.run {
			removePower(removeRemaining)
			removeRemaining = 0
		}

		return removeRemaining
	}

	private fun getAvailablePower(): Int = extractorNode.extractableNodes
		.flatMap { it.getPoweredMultiblocks() }
		.distinct()
		.filterNot { (it as MultiblockEntity).removed }
		.sumOf { it.getPower() }

	override fun getTransferPower(destination: PoweredMultiblockEntity): Int {
		val destinationCapacity = destination.maxPower - destination.getPower()
		println("Room: $destinationCapacity")
		println("Available: ${getAvailablePower()}")
		println("Transfer limit: ${extractorNode.getTransferPower()}")

		return minOf(destinationCapacity, transferLimit, getAvailablePower())
	}
}
