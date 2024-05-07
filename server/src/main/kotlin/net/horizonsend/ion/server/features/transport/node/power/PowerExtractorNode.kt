package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerExtractorNode() : SingleNode {
	constructor(position: BlockKey) : this() {
		this.position = position
	}

	override var position by Delegates.notNull<Long>()
	override val transferableNeighbors: MutableSet<TransportNode> = ObjectOpenHashSet()

	override fun isTransferable(position: Long, node: TransportNode): Boolean {
		return node !is PowerInputNode
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	override fun toString(): String = """
		POWER INPUT NODE:
		Transferable to: ${transferableNeighbors.joinToString { it.javaClass.simpleName }} nodes
	""".trimIndent()
}

