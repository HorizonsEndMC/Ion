package net.horizonsend.ion.server.features.transport.network

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.network.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.NetworkType
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import java.util.concurrent.ConcurrentHashMap

abstract class TransportNetwork(val holder: NetworkHolder<*>) {
	var ready: Boolean = false

	val nodes: ConcurrentHashMap<Long, TransportNode> = ConcurrentHashMap()

	val world get() = holder.getWorld()

	abstract val namespacedKey: NamespacedKey
	abstract val type: NetworkType
	abstract val nodeFactory: NodeFactory<*>
	abstract val dataVersion: Int

	open fun processBlockRemoval(key: BlockKey) { holder.scope.launch { withTransportDisabled {
		val previousNode = nodes[key] ?: return@withTransportDisabled

		previousNode.handleRemoval(key)
	}}}

	open fun processBlockRemovals(keys: Iterable<BlockKey>) { holder.scope.launch { withTransportDisabled {
		for (key in keys) {
			val previousNode = nodes[key] ?: return@withTransportDisabled

			previousNode.handleRemoval(key)
		}
	}}}

	open fun processBlockAddition(new: BlockSnapshot) { holder.scope.launch {
		if (new.type.isAir) {
			processBlockRemoval(toBlockKey(new.x, new.y, new.z))

			return@launch
		}

		withTransportDisabled { createNodeFromBlock(new) }
	}}

	open fun processBlockAdditions(changes: Iterable<BlockSnapshot>) { holder.scope.launch { withTransportDisabled {
		for (new in changes) {
			createNodeFromBlock(new)
		}
	}}}

	/**
	 * Handle the creation / loading of the node into memory
	 **/
	suspend fun createNodeFromBlock(block: BlockSnapshot) {
		val key = toBlockKey(block.x, block.y, block.z)

		withTransportDisabled { nodeFactory.create(key, block) }
	}

	/**
	 * Save additional metadata into the network PDC
	 **/
	open fun saveAdditional(pdc: PersistentDataContainer) {}

	suspend fun tickIfReady() {
		if (ready) tick()
	}

	abstract suspend fun tick()

	abstract fun clearData()

	/**
	 * Logic for when the holding chunk is unloaded
	 **/
	fun onUnload() {
		// Break cross chunk relations
		breakAllRelations()
	}

	/**
	 * Get the neighbors of a node
	 **/
	suspend fun buildRelations() {
		for ((key, node) in nodes) {
			node.buildRelations(key)
		}
	}

	/**
	 * Handles any cleanup tasks at the end of loading
	 **/
	open fun finalizeNodes() {}

	fun breakAllRelations() {
		runBlocking { nodes.values.forEach { it.clearRelations() } }
	}

	fun getNode(x: Int, y: Int, z: Int, allowNeighborChunks: Boolean = true): TransportNode? {
		val key = toBlockKey(x, y, z)
		return getNode(key, allowNeighborChunks)
	}

	/**
	 * Gets a node from this chunk, or a direct neighbor, if loaded
	 **/
	fun getNode(key: BlockKey, allowNeighborChunks: Boolean = true): TransportNode? {
		return if (allowNeighborChunks) holder.getGlobalNode(key) else holder.getInternalNode(key)
	}

	protected suspend inline fun withTransportDisabled(crossinline block: suspend () -> Unit) {
		ready = false

		try {
			block.invoke()
		} finally {
			ready = true
		}
	}
}
