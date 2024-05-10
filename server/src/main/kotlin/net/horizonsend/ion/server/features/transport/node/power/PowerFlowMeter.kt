package net.horizonsend.ion.server.features.transport.node.power

import com.google.common.collect.EvictingQueue
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.features.transport.step.TransportStep
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.kyori.adventure.text.Component
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerFlowMeter(override val network: ChunkPowerNetwork) : SingleNode {
	// The position will always be set
	override var position by Delegates.notNull<Long>()

	constructor(network: ChunkPowerNetwork, position: BlockKey) : this(network) {
		this.position = position
	}

	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	override suspend fun handleStep(step: Step) {
		// This is not an origin node, so we can assume that it is not an origin step
		step as TransportStep

		val next = getTransferableNodes()
			.filterNot { step.traversedNodes.contains(it) }
			.filterNot { step.previous.currentNode == it }
			.randomOrNull() ?: return

		println("Next node is $next")

		// Simply move on to the next node
		TransportStep(
			step.origin,
			step.steps,
			next,
			step,
			step.traversedNodes
		).invoke()
	}

	private var lastStepped: Long = System.currentTimeMillis()
	private var steps: Int = 1
	private val averages = EvictingQueue.create<Double>(10)

	override suspend fun onCompleteChain(final: Step, destination: PowerInputNode, transferred: Int) {
		final as TransportStep

		val time = System.currentTimeMillis()
		val diff = time - lastStepped
		lastStepped = time

		steps++

		val seconds = diff / 1000.0
		averages.add(transferred / seconds)

		network.world.sendMessage(Component.text("Running average transferred is ${averages.average()}"))
	}
}
