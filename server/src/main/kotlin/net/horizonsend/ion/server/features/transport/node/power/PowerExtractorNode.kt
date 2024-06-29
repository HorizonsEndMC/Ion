package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.head.power.SinglePowerBranchHead
import net.horizonsend.ion.server.features.transport.step.new.NewStep
import net.horizonsend.ion.server.features.transport.step.origin.ExtractorPowerOrigin
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin
import net.horizonsend.ion.server.features.transport.step.result.MoveForward
import net.horizonsend.ion.server.features.transport.step.result.StepResult
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class PowerExtractorNode(override val network: ChunkPowerNetwork) : SingleNode, SourceNode<ChunkPowerNetwork> {
	override var position by Delegates.notNull<Long>()

	constructor(network: ChunkPowerNetwork, position: BlockKey) : this(network) {
		this.position = position
		network.extractors[position] = this
	}

	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()
	val extractableNodes: MutableSet<PowerInputNode> get() = relationships.mapNotNullTo(mutableSetOf()) { it.sideTwo.node as? PowerInputNode }

	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is PowerInputNode) return false
		return node !is SourceNode<*>
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	override fun loadIntoNetwork() {
		super.loadIntoNetwork()
		network.extractors[position] = this
	}

	override suspend fun handleRemoval(position: BlockKey) {
		network.extractors.remove(position)
		super.handleRemoval(position)
	}

	override suspend fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = network.getNode(offsetKey) ?: continue

			if (this == neighborNode) return

			if (neighborNode is PowerInputNode) {
				extractableNodes.add(neighborNode)
			}

			// Add a relationship, if one should be added
			addRelationship(neighborNode)
		}
	}

	private var lastTicked: Long = System.currentTimeMillis()
	fun markTicked() {
		lastTicked = System.currentTimeMillis()
	}

	override suspend fun startStep(): NewStep<ChunkPowerNetwork>? {
		if (extractableNodes.isEmpty()) return null

		val extractablePowerPool = extractableNodes.flatMap { it.getPoweredMultiblocks() }
		if (extractablePowerPool.all { it.isEmpty() }) return null

		val step =  NewStep(
			network = this.network,
			origin = getOriginData() ?: return null
		) {
			SinglePowerBranchHead(
				holder = this,
				currentNode = this@PowerExtractorNode,
				share = 1.0,
			)
		}

		markTicked()

		return step
	}

	override suspend fun getNextNode(head: BranchHead<ChunkPowerNetwork>): TransportNode? {
		return getTransferableNodes().randomOrNull()
	}

	override suspend fun handleHeadStep(head: BranchHead<ChunkPowerNetwork>): StepResult<ChunkPowerNetwork> {
		return MoveForward()
	}

	override suspend fun getOriginData(): StepOrigin<ChunkPowerNetwork>? {
		if (getTransferableNodes().isEmpty()) return null
		return ExtractorPowerOrigin(this)
	}

	fun getTransferPower(): Int {
		val interval = IonServer.transportSettings.extractorTickIntervalMS.toDouble()
		println("Tick Interval: $interval")
		println("MaxPerExtractorTick: ${IonServer.transportSettings.maxPowerRemovedPerExtractorTick}")
		println("Delta MS: ${System.currentTimeMillis() - lastTicked}")

		return (IonServer.transportSettings.maxPowerRemovedPerExtractorTick * ((System.currentTimeMillis() - lastTicked) / interval)).roundToInt()
	}

	override fun toString(): String = "POWER Extractor NODE: Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes"
}

