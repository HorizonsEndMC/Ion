package net.horizonsend.ion.server.features.transport.node.manager

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.server.features.transport.node.NetworkType
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
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

abstract class NodeManager(val holder: NetworkHolder<*>) {
	val nodes: ConcurrentHashMap<Long, TransportNode> = ConcurrentHashMap()

	val world get() = holder.getWorld()

	abstract val namespacedKey: NamespacedKey
	abstract val type: NetworkType
	abstract val nodeFactory: NodeFactory<*>
	abstract val dataVersion: Int

	open fun processBlockRemoval(key: BlockKey) { holder.scope.launch {
		val previousNode = nodes[key] ?: return@launch

		previousNode.handleRemoval(key)
		holder.markUnsaved()
	}}

	open fun processBlockRemovals(keys: Iterable<BlockKey>) { holder.scope.launch {
		var hits: Int = 0

		for (key in keys) {
			val previousNode = nodes[key] ?: return@launch
			hits++

			previousNode.handleRemoval(key)
		}

		if (hits > 0) holder.markUnsaved()
	}}

	open fun processBlockChange(new: Block) { holder.scope.launch {
		if (new.type.isAir) {
			processBlockRemoval(toBlockKey(new.x, new.y, new.z))

			return@launch
		}

		if (createNodeFromBlock(new)) holder.markUnsaved()
	}}

	open fun processBlockChange(position: BlockKey) { holder.scope.launch {
		val block = world.getBlockAt(getX(position), getY(position), getZ(position))

		if (block.type.isAir) {
			processBlockRemoval(position)

			return@launch
		}

		if (createNodeFromBlock(block)) holder.markUnsaved()
	}}

	open fun processBlockChange(position: BlockKey, data: BlockData) { holder.scope.launch {
		if (data.material.isAir) {
			processBlockRemoval(position)

			return@launch
		}

		if (createNodeFromBlock(position, data)) holder.markUnsaved()
	}}

	open fun processBlockChanges(changeMap: Map<BlockKey, BlockData>) { holder.scope.launch {
		for ((position, data) in changeMap) {
			if (data.material.isAir) {
				processBlockRemoval(position)

				return@launch
			}

			if (createNodeFromBlock(position, data)) holder.markUnsaved()
		}
	}}

	open fun processBlockAdditions(changed: Iterable<Block>) { holder.scope.launch {
		var hits = 0
		for (new in changed) {
			if (createNodeFromBlock(new)) {
				hits++
			}
		}

		if (hits > 0) holder.markUnsaved()
	}}

	/**
	 * Handle the creation / loading of the node into memory
	 **/
	suspend fun createNodeFromBlock(block: Block): Boolean {
		val key = toBlockKey(block.x, block.y, block.z)

		return nodeFactory.create(key, block.blockData)
	}

	suspend fun createNodeFromBlock(position: BlockKey, data: BlockData): Boolean {
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
}
