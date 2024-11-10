package net.horizonsend.ion.server.features.transport.node.manager

import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.type.general.ExtractorNode
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.features.transport.node.util.NodeFactory
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.persistence.PersistentDataContainer
import java.util.concurrent.ConcurrentHashMap

abstract class NodeManager<T: ExtractorNode>(val holder: NetworkHolder<*>) {
	val nodes: ConcurrentHashMap<Long, TransportNode> = ConcurrentHashMap()
	val extractors: ConcurrentHashMap<BlockKey, T> = ConcurrentHashMap()

	val world get() = holder.getWorld()

	abstract val namespacedKey: NamespacedKey
	abstract val type: NetworkType
	abstract val nodeFactory: NodeFactory<*>
	abstract val dataVersion: Int

	var ready: Boolean = false

	/**
	 * Handle the creation / loading of the node into memory
	 **/
	fun createNodeFromBlock(block: Block): Boolean {
		val key = toBlockKey(block.x, block.y, block.z)

		return nodeFactory.create(key, block.blockData)
	}

	fun createNodeFromBlock(position: BlockKey, data: BlockData): Boolean {
		return nodeFactory.create(position, data)
	}

	/**
	 * Save additional metadata into the network PDC
	 **/
	open fun saveAdditional(pdc: PersistentDataContainer) {}

	abstract fun clearData()

	/**
	 * Logic for when the holding chunk is unloaded
	 **/
	fun onUnload() {
		// Break cross chunk relations
		Tasks.async { breakAllRelations() }
	}

	/**
	 * Get the neighbors of a node
	 **/
	fun buildRelations() {
		for ((key, node) in nodes) {
			node.buildRelations(key)
		}
	}

	/**
	 * Handles any cleanup tasks at the end of loading
	 **/
	open fun finalizeNodes() {}

	private fun breakAllRelations() {
		nodes.values.forEach { it.clearRelations() }
	}

	fun getNode(x: Int, y: Int, z: Int, allowNeighborChunks: Boolean = true): TransportNode? {
		val key = toBlockKey(x, y, z)
		return getNode(key, allowNeighborChunks)
	}

	/**
	 * Gets a node from this chunk, or a direct neighbor, if loaded
	 **/
	fun getNode(key: BlockKey, allowNeighborChunks: Boolean = true): TransportNode? {
		return null
	}
}
