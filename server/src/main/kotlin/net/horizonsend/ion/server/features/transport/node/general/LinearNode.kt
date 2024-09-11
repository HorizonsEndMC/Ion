package net.horizonsend.ion.server.features.transport.node.general

import com.manya.pdc.base.EnumDataType
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.faces
import org.bukkit.Axis
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

abstract class LinearNode<T: TransportNetwork, A: LinearNode<T, B, A>, B: LinearNode<T, A, B>>(override val network: T) : MultiNode<B, A> {
	override var isDead: Boolean = false
	override val positions: MutableSet<Long> = LongOpenHashSet()
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()
	var axis by Delegates.notNull<Axis>()

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is PowerExtractorNode && node !is SolarPanelNode
	}

	abstract suspend fun addBack(position: BlockKey)

	override suspend fun rebuildNode(position: BlockKey) {
		// Create new nodes, automatically merging together
		positions.forEach {
			addBack(it)
		}

		// Handle relations once fully rebuilt
		positions.forEach {
			network.nodes[it]?.buildRelations(it)
		}
	}

	override suspend fun buildRelations(position: BlockKey) {
		for (offset in axis.faces.toList()) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = network.getNode(offsetKey) ?: continue

			if (this == neighborNode) continue

			addRelationship(neighborNode, offset)
		}
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }

		axis = persistentDataContainer.getOrDefault(NamespacedKeys.AXIS, EnumDataType(Axis::class.java), Axis.Y)
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, positions.toLongArray())
		persistentDataContainer.set(NamespacedKeys.AXIS, EnumDataType(Axis::class.java), axis)
	}

	override fun toString(): String = "(END ROD NODE: Axis: $axis; ${positions.size} positions; Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes)"
}
