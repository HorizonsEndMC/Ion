package net.horizonsend.ion.server.features.transport.node.power

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.node.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerDirectionalNode(override val manager: PowerNodeManager) : SingleNode() {
	override val type: NodeType = NodeType.POWER_DIRECTIONAL_NODE
	private var variant: Material by Delegates.notNull()

	constructor(network: PowerNodeManager, position: BlockKey, variant: Material) : this(network) {
		this.position = position
		this.variant = variant
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is SpongeNode) return false
		return node !is PowerExtractorNode && node !is SolarPanelNode
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
		persistentDataContainer.set(NamespacedKeys.NODE_VARIANT, materialDataType, variant)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
		variant = persistentDataContainer.get(NamespacedKeys.NODE_VARIANT, materialDataType)!!
	}

	companion object {
		val materialDataType = EnumDataType(Material::class.java)
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 1
	}
}
