package net.horizonsend.ion.server.features.transport.step.origin

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode

class ExtractorPowerOrigin(
	val origin: PowerExtractorNode
) : StepOrigin<ChunkPowerNetwork>, PowerOrigin {
	/**
	 * Remove power from the origin extractor's multiblocks.
	 *
	 * @return the amount of power that could not be removed
	 **/
	fun removeOrigin(amount: Int): Int {
		// List of multiblock entities that power may be taken from
		val availableEntities = origin.extractableNodes
			.flatMap { it.getPoweredMultiblocks() }
			.distinct()
			.filterNotTo(mutableListOf()) { (it as MultiblockEntity).removed || it.isEmpty() }

		val transportLimit = origin.getTransferPower()

		var removeRemaining = amount

		while (removeRemaining >= 0) {
			val minRemove = availableEntities.minOfOrNull { it.getPower() } ?: break

			// Remove the minimum in any of the multis, the limit of what can be removed, or the remaining power to remove.
			// Whichever is lowest.
			val toRemove = minOf(minRemove, transportLimit, removeRemaining)

			val share = toRemove / availableEntities.size
			val remainder = toRemove - (share * availableEntities.size)

			// Remove an equal share from each of the inventories
			for (entity in availableEntities) {
				entity as MultiblockEntity
				if (entity.removed) continue

				// Should never be more than 0, but handle the possibility
				val notRemoved = entity.removePower(share)
				removeRemaining -= (notRemoved - share)
			}

			// Remove the remainder, if able
			// If not able, it can just be stuck onto the amount that couldn't be removed
			availableEntities.firstOrNull { it.canRemovePower(remainder) }?.run {
				removePower(remainder)
				removeRemaining -= remainder
			}

			availableEntities.removeAll { it.isEmpty() }
		}

		return removeRemaining
	}

	private fun getAvailablePower(): Int = origin.extractableNodes
		.flatMap { it.getPoweredMultiblocks() }
		.distinct()
		.filterNot { (it as MultiblockEntity).removed }
		.sumOf { it.getPower() }

	fun finish() {
		origin.markTicked()
	}

	override fun getTransferPower(destination: PoweredMultiblockEntity): Int {
		val room = destination.maxPower - destination.getPower()
		return minOf(room, origin.getTransferPower(), getAvailablePower())
	}
}
