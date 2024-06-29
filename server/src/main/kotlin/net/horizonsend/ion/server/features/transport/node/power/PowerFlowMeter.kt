package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.node.type.StepHandler
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.result.MoveForward
import net.horizonsend.ion.server.features.transport.step.result.StepResult
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.text.Component
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerFlowMeter(override val network: ChunkPowerNetwork) : SingleNode, StepHandler<ChunkPowerNetwork> {
	constructor(network: ChunkPowerNetwork, position: BlockKey) : this(network) {
		this.position = position
	}

	override var position by Delegates.notNull<Long>()
	override var isDead: Boolean = false
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	/*
	 * Should transfer power like any normal node.
	 *
	 * And it cannot transfer into a source
	 */
	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode<*>
	}

	override suspend fun handleHeadStep(head: BranchHead<ChunkPowerNetwork>): StepResult<ChunkPowerNetwork> {
		// Simply move on to the next node
		return MoveForward()
	}

	override suspend fun getNextNode(head: BranchHead<ChunkPowerNetwork>): TransportNode? = getTransferableNodes()
		.filterNot { head.previousNodes.contains(it) }
		.randomOrNull()

	private val STORED_AVERAGES = 20
	private val averages = mutableListOf<TransferredPower>()

	override suspend fun onCompleteChain(final: BranchHead<*>, destination: PowerInputNode, transferred: Int) {
		addAverage(TransferredPower(transferred, System.currentTimeMillis()))
		network.world.sendMessage(Component.text("${toVec3i(position)} ${network.world.name} Running average transferred is ${calculateAverage()}"))
	}

	private fun addAverage(average: TransferredPower) {
		val currentSize = averages.size

		if (currentSize < STORED_AVERAGES) {
			averages.add(average)
			return
		}

		// If it is full, shift all averages to the right
		for (index in 18 downTo 0) {
			averages[index + 1] = averages[index]
		}

		averages[0] = average
	}

	private fun calculateAverage(): Double {
		val sum = averages.sumOf { it.transferred }

		val timeDiff = (System.currentTimeMillis() - averages.minOf { it.time }) / 1000.0

		return sum / timeDiff
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	private data class TransferredPower(val transferred: Int, val time: Long)
}
