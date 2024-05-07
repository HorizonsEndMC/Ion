package net.horizonsend.ion.server.features.transport.grid

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.power.PowerNodeFactory
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.power.TransportNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.filterValuesIsInstance
import org.bukkit.NamespacedKey
import java.util.concurrent.ConcurrentHashMap

class ChunkPowerNetwork(manager: ChunkTransportManager) : ChunkTransportNetwork(manager) {
	val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()
//	val extractors = ConcurrentHashMap<Long, PowerExtractorNode>()

	override val namespacedKey: NamespacedKey = NamespacedKeys.POWER_TRANSPORT
	override val nodeFactory: PowerNodeFactory = PowerNodeFactory(this)
	val solarPanels: ObjectOpenHashSet<SolarPanelNode> = ObjectOpenHashSet()

	override fun setup() {
		collectPowerMultiblockEntities()
	}

	override fun processBlockRemoval(key: Long) { manager.scope.launch {
		val previousNode = nodes[key] ?: return@launch

		previousNode.handleRemoval(this@ChunkPowerNetwork, key)
	}}

	override fun processBlockAddition(key: Long, new: BlockSnapshot) { manager.scope.launch {
		createNodeFromBlock(new)
	}}

	fun tickSolars() {
		for ((key, solarPanel) in nodes.filterValuesIsInstance<SolarPanelNode, BlockKey, TransportNode>()) {
			val power = solarPanel.getPower(this)
			solarPanel.lastTicked = System.currentTimeMillis()
			println("Solar panel generated $power power!")

			transferPower(solarPanel, power)
		}
	}

	fun transferPower(start: TransportNode, amount: Int) {
		var steps: Int = 0

		var currentNode = start

		while (steps < 10) {
			steps++

			if (currentNode is PowerInputNode) {
				currentNode.multis.randomOrNull()?.addPower(amount)

				break
			}

			currentNode = currentNode.transferableNeighbors.randomOrNull()?: break
		}
	}

	override fun tick() {
		tickSolars()
//		tickExecutor()
	}

	private fun collectPowerMultiblockEntities() {
		manager.chunk.multiblockManager.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}
	}
}
