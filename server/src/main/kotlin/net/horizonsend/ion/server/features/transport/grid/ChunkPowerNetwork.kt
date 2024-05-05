package net.horizonsend.ion.server.features.transport.grid

import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.features.transport.node.power.PowerNodeFactory
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.IntervalExecutor
import org.bukkit.NamespacedKey
import java.util.concurrent.ConcurrentHashMap

class ChunkPowerNetwork(manager: ChunkTransportManager) : ChunkTransportNetwork(manager) {
	val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()
//	val extractors = ConcurrentHashMap<Long, PowerExtractorNode>()

	override val namespacedKey: NamespacedKey = NamespacedKeys.POWER_TRANSPORT
	override val nodeFactory: NodeFactory<*> = PowerNodeFactory

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

	fun removeNode(key: Long) {
		nodes.remove(key)
	}

	val tickExecutor = IntervalExecutor(20) {

	}

	override fun tick() {
//		tickExecutor()
	}

	private fun collectPowerMultiblockEntities() {
		manager.chunk.multiblockManager.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}
	}
}