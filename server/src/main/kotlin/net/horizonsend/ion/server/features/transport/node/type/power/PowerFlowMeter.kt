package net.horizonsend.ion.server.features.transport.node.type.power

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.PowerFlowMeterDisplay
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class PowerFlowMeter(override val manager: PowerNodeManager) : SingleNode(), PowerPathfindingNode {
	override val type: NodeType = NodeType.POWER_FLOW_METER
	constructor(network: PowerNodeManager, position: BlockKey, direction: BlockFace) : this(network) {
		this.position = position
		this.direction = direction
	}

	lateinit var direction: BlockFace

	/*
	 * Should transfer power like any normal node.
	 *
	 * And it cannot transfer into a source
	 */
	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is PowerExtractorNode && node !is SolarPanelNode
	}

	private val STORED_AVERAGES = 20
	private val averages = mutableListOf<TransferredPower>()

	fun onCompleteChain(transferred: Int) {
		addTransferred(TransferredPower(transferred, System.currentTimeMillis()))
		if (::displayHandler.isInitialized) displayHandler.update()
	}

	companion object {
		val firstLine = text("E: ", YELLOW)
		val secondLine = ofChildren(newline(), text("E ", YELLOW), text("/ ", HE_MEDIUM_GRAY), text("Second", GREEN))
	}

	fun formatPower(): Component {
		var avg = runCatching { calculateAverage().roundToHundredth() }.getOrDefault(0.0)

		// If no averages, or no power has been moved in 5 seconds, go to 0
		if (averages.isEmpty() || System.currentTimeMillis() - averages.maxOf { it.time } > 5000) {
			avg = 0.0
		}

		return ofChildren(firstLine, text(avg, GREEN), secondLine)
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

	lateinit var displayHandler: TextDisplayHandler

	private fun setupDisplayEntity() {
		displayHandler = DisplayHandlers.newBlockOverlay(
			manager.world,
			toVec3i(position),
			direction,
			PowerFlowMeterDisplay(this, 0.0, 0.0, 0.0, 0.7f)
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

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
		persistentDataContainer.set(NamespacedKeys.AXIS, EnumDataType(BlockFace::class.java), direction)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
		direction = persistentDataContainer.getOrDefault(NamespacedKeys.AXIS, EnumDataType(BlockFace::class.java), NORTH)
	}

	private data class TransferredPower(val transferred: Int, val time: Long)

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 1
	}

	override fun getNextNodes(previous: TransportNode): ArrayDeque<TransportNode> = cachedTransferable
}
