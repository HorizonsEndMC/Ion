package net.horizonsend.ion.server.features.transport.node.power

import com.manya.pdc.base.EnumDataType
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.server.features.client.display.container.TextDisplayHandler
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
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
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerFlowMeter(override val network: ChunkPowerNetwork) : SingleNode, StepHandler<ChunkPowerNetwork> {
	constructor(network: ChunkPowerNetwork, position: BlockKey, direction: BlockFace) : this(network) {
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

	override suspend fun handleHeadStep(head: SingleBranchHead<ChunkPowerNetwork>): StepResult<ChunkPowerNetwork> {
		// Simply move on to the next node
		return MoveForward()
	}

	override suspend fun getNextNode(head: SingleBranchHead<ChunkPowerNetwork>, entranceDirection: BlockFace): Pair<TransportNode, BlockFace>? = getTransferableNodes()
		.filterNot { head.previousNodes.contains(it.first) }
		.randomOrNull()

	private val STORED_AVERAGES = 20
	private val averages = mutableListOf<TransferredPower>()

	override suspend fun onCompleteChain(final: BranchHead<*>, destination: PowerInputNode, transferred: Int) {
		addTransferred(TransferredPower(transferred, System.currentTimeMillis()))

		val avg = runCatching { calculateAverage().roundToHundredth() }.getOrDefault(0.0)
		displayHandler.setText(text(avg, GREEN))
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

	private lateinit var displayHandler: TextDisplayHandler

	private fun setupDisplayEntity() {
		// 95% of the way to the edge of the block once added to the center of the block, to avoid z fighting
		val offset = direction.direction.multiply(0.45)

		val facingBlock = getRelative(position, direction)

		val x = getX(facingBlock).toDouble() + 0.5 - offset.x
		val y = getY(facingBlock).toDouble() + 0.5
		val z = getZ(facingBlock).toDouble() + 0.5 - offset.z

		displayHandler = TextDisplayHandler(
			network.world,
			x,
			y,
			z,
			1.0f,
			direction
		)
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
