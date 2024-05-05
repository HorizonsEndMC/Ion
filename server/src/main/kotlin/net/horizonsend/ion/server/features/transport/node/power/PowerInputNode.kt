package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK_DESTINATIONS
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerInputNode() : SingleNode {
	constructor(position: BlockKey) : this() {
		this.position = position
	}

	override var position by Delegates.notNull<Long>()
	override val transferableNeighbors: MutableSet<TransportNode> = ObjectOpenHashSet()

	/**
	 * Multiblocks that share this power input
	 **/
	val multiblockPositions: MutableSet<Long> = LongOpenHashSet()

	override fun isTransferable(position: Long, node: TransportNode): Boolean {
		TODO("Not yet implemented")
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
		persistentDataContainer.set(MULTIBLOCK_DESTINATIONS, PersistentDataType.LONG_ARRAY, multiblockPositions.toLongArray())
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!

		multiblockPositions.addAll(persistentDataContainer.getOrDefault(NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, longArrayOf()).toList())
	}
}
