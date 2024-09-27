package net.horizonsend.ion.server.features.transport.node.type.general

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.FlowMeterDisplay
import net.horizonsend.ion.server.features.transport.node.type.power.PowerPathfindingNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.text.Component

abstract class FlowMeter : DirectionalNode(), PowerPathfindingNode {
	private val STORED_AVERAGES = 20
	protected val averages = mutableListOf<TransferredPower>()

	abstract fun formatFlow(): Component

	fun onCompleteChain(transferred: Int) {
		addTransferred(TransferredPower(transferred, System.currentTimeMillis()))
		if (::displayHandler.isInitialized) displayHandler.update()
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

	protected fun calculateAverage(): Double {
		val sum = averages.sumOf { it.transferred }

		val timeDiff = (System.currentTimeMillis() - averages.minOf { it.time }) / 1000.0

		return sum / timeDiff
	}

	private lateinit var displayHandler: TextDisplayHandler

	private fun setupDisplayEntity() {
		displayHandler = DisplayHandlers.newBlockOverlay(
			manager.world,
			toVec3i(position),
			direction,
			FlowMeterDisplay(this, 0.0, 0.0, 0.0, 0.7f)
		).register()
	}

	override fun loadIntoNetwork() {
		setupDisplayEntity()

		super.loadIntoNetwork()
	}

	override fun onPlace(position: BlockKey) {
		runCatching { setupDisplayEntity() }

		super.onPlace(position)
	}

	override fun handlePositionRemoval(position: BlockKey) {
		if (::displayHandler.isInitialized) displayHandler.remove()

		super.handlePositionRemoval(position)
	}

	protected data class TransferredPower(val transferred: Int, val time: Long)
}
