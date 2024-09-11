package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class InvertedDirectionalNode(override val network: PowerNetwork) : SingleNode {
	override var isDead: Boolean = false
	override var position: BlockKey by Delegates.notNull()
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	constructor(network: PowerNetwork, position: BlockKey) : this(network) {
		this.position = position
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is EndRodNode) return false
		return node !is PowerExtractorNode && node !is SolarPanelNode
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}
}
