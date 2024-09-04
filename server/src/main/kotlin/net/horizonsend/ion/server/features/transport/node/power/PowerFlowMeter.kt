package net.horizonsend.ion.server.features.transport.node.power

import com.manya.pdc.base.EnumDataType
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.PowerFlowMeterDisplay
import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.node.type.StepHandler
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.head.SingleBranchHead
import net.horizonsend.ion.server.features.transport.step.result.MoveForward
import net.horizonsend.ion.server.features.transport.step.result.StepResult
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
import kotlin.properties.Delegates

class PowerFlowMeter(override val network: PowerNetwork) : SingleNode, StepHandler<PowerNetwork> {
	constructor(network: PowerNetwork, position: BlockKey, direction: BlockFace) : this(network) {
		this.position = position
		this.direction = direction
	}

	override var position by Delegates.notNull<Long>()
	override var isDead: Boolean = false
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()
	var direction: BlockFace by Delegates.notNull()

	/*
	 * Should transfer power like any normal node.
	 *
	 * And it cannot transfer into a source
	 */
	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode<*>
	}

	override suspend fun handleHeadStep(head: SingleBranchHead<PowerNetwork>): StepResult<PowerNetwork> {
		// Simply move on to the next node
		return MoveForward()
	}

	override suspend fun getNextNode(head: SingleBranchHead<PowerNetwork>, entranceDirection: BlockFace): Pair<TransportNode, BlockFace>? = getTransferableNodes()
		.filterNot { head.previousNodes.contains(it.first) }
		.randomOrNull()

	private val STORED_AVERAGES = 20
	private val averages = mutableListOf<TransferredPower>()

	override suspend fun onCompleteChain(final: BranchHead<*>, destination: PowerInputNode, transferred: Int) {
		addTransferred(TransferredPower(transferred, System.currentTimeMillis()))
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
			network.world,
			toVec3i(position),
			direction,
			PowerFlowMeterDisplay(this, 0.0, 0.0, 0.0, direction, 0.7f)
		).register()
	}

	override fun loadIntoNetwork() {
		setupDisplayEntity()

		super.loadIntoNetwork()
	}

	override suspend fun onPlace(position: BlockKey) {
		setupDisplayEntity()

		super.onPlace(position)
	}

	override suspend fun handleRemoval(position: BlockKey) {
		if (::displayHandler.isInitialized) displayHandler.remove()

		super.handleRemoval(position)
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
}
