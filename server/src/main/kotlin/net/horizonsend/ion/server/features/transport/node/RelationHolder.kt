package net.horizonsend.ion.server.features.transport.node

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.BlockFace
import java.util.concurrent.BlockingDeque
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque

class RelationHolder(val node: TransportNode) {
	private val relationships = ConcurrentHashMap<BlockKey, BlockingDeque<NodeRelationship>>()
	private val containsCache = ConcurrentHashMap<BlockKey, IntOpenHashSet>()

	/**
	 * Create a relationship between this node and the provided node
	 *
	 * If neither side can transfer, a relation will not be created
	 **/
	fun addRelationship(other: TransportNode, holderPosition: BlockKey, otherPosition: BlockKey, nodeTwoOffset: BlockFace) {
		// Do not add duplicates
		val existingAt = relationships[holderPosition]
		if (existingAt != null && existingAt.any { it.other == other }) return

		create(other, holderPosition, otherPosition, nodeTwoOffset)
		other.neighborChanged(node)
	}

	/**
	 * @param other The node this relation is being created with
	 * @param holderPosition The position that this node was created from
	 * @param otherPosition The position that the other node was found at
	 * @param nodeTwoOffset The direction that the other node was found
	 **/
	private fun create(other: TransportNode, holderPosition: BlockKey, otherPosition: BlockKey, nodeTwoOffset: BlockFace) {
		val holderToOther = node.isTransferableTo(other)
		val otherToHolder = other.isTransferableTo(node)

		// Add relation from this node to the other
		add(holderPosition, NodeRelationship(node, other, nodeTwoOffset, holderToOther))
		// Add relation from the other to this
		other.relationHolder.add(otherPosition, NodeRelationship(other, node, nodeTwoOffset.oppositeFace, otherToHolder))

		node.refreshTransferCache()
		other.refreshTransferCache()
	}

	fun add(point: BlockKey, relation: NodeRelationship) {
		relationships.getOrPut(point) { LinkedBlockingDeque() }.add(relation)
		containsCache.getOrPut(point) { IntOpenHashSet() }.add(relation.other.hashCode())
	}

	fun remove(point: BlockKey, relation: NodeRelationship) {
		relationships[point]?.remove(relation)
		containsCache[point]?.remove(relation.other.hashCode())
	}

	fun removeAll(point: BlockKey): BlockingDeque<NodeRelationship>? {
		return relationships.remove(point)
	}

	fun clear() {
		relationships.values.forEach {
			for (nodeRelationship in it) {
				nodeRelationship.breakUp()
			}
		}
	}

	fun removeRelationship(other: TransportNode) {
		// Handle duplicate cases
		for (key in relationships.keys.filter { containsCache.contains(other.hashCode()) }) {
			relationships[key]?.removeAll { it.other == other }
		}

		// Notify of neighbor change
		other.neighborChanged(node)
	}

	fun getAllOthers(): Set<NodeRelationship> {
		val others = ObjectOpenHashSet<NodeRelationship>()

		for (key in relationships.keys) {
			for (relation in relationships[key]!!) {
				others.add(relation)
			}
		}

		return others
	}

	fun raw() = relationships

	fun hasRelationAt(position: BlockKey): Boolean = relationships.containsKey(position)
	fun hasRelationAtWith(point: BlockKey, other: TransportNode): Boolean = containsCache[point]?.contains(other.hashCode()) ?: false
}
