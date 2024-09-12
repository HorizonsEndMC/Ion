package net.horizonsend.ion.server.features.transport.node.general

import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

abstract class JunctionNode<T: NodeManager, A: JunctionNode<T, B, A>, B: JunctionNode<T, A, B>>(override val manager: T) : MultiNode<B, A>() {
	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is PowerExtractorNode && node !is SolarPanelNode
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, positions.toLongArray())
	}
}
