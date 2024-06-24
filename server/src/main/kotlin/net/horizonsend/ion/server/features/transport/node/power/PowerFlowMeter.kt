package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.IntermediateNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.result.MoveForward
import net.horizonsend.ion.server.features.transport.step.result.StepResult
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.kyori.adventure.text.Component
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerFlowMeter(override val network: ChunkPowerNetwork) : SingleNode, IntermediateNode<ChunkPowerNetwork> {
	// The position will always be set
	override var position by Delegates.notNull<Long>()

	constructor(network: ChunkPowerNetwork, position: BlockKey) : this(network) {
		this.position = position
	}

	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode<*>
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	override suspend fun handleHeadStep(head: BranchHead<ChunkPowerNetwork>): StepResult<ChunkPowerNetwork> {
		// Simply move on to the next node
		return MoveForward()
	}

	override suspend fun getNextNode(head: BranchHead<ChunkPowerNetwork>): TransportNode? = getTransferableNodes()
		.filterNot { head.previousNodes.contains(it) }
		.randomOrNull()

	private var lastStepped: Long = System.currentTimeMillis()

	private val STORED_AVERAGES = 20
	private val averages = mutableListOf<TransferredPower>()

	override suspend fun onCompleteChain(final: BranchHead<*>, destination: PowerInputNode, transferred: Int) {
		addAverage(TransferredPower(transferred, System.currentTimeMillis()))

		network.world.sendMessage(Component.text("Running average transferred is ${calculateAverage()}"))
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

	fun calculateAverage(): Double {
//		println("Averages: $averages")

		val last = averages.first()

//		println("Last: $last")

		val sum = averages.sumOf { it.transferred }

//		println("Transferred sum: $sum")

		val timeDiff = (System.currentTimeMillis() - averages.minOf { it.time }) / 1000.0

//		println("Seconds diff $timeDiff")

		return sum / timeDiff
	}

	private data class TransferredPower(val transferred: Int, val time: Long)
}
