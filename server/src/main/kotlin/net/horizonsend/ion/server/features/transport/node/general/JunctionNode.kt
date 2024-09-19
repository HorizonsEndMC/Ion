package net.horizonsend.ion.server.features.transport.node.general

import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.node.NodeManager
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.averageBy
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.math.roundToInt

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

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		if (previousNode != null) {
			if (nextNode != null) {
				// Provided both means it needs to be more precise for an accurate distribution of resources. Get in / out locations via relations
				val relationPrevious = getRelationshipWith(previousNode)
				val relationNext = getRelationshipWith(previousNode)

				val previousVec = Vec3i(
					relationPrevious.keys.averageBy { getX(it).toDouble() }.roundToInt(),
					relationPrevious.keys.averageBy { getY(it).toDouble() }.roundToInt(),
					relationPrevious.keys.averageBy { getZ(it).toDouble() }.roundToInt()
				)

				val nextVec = Vec3i(
					relationNext.keys.averageBy { getX(it).toDouble() }.roundToInt(),
					relationNext.keys.averageBy { getY(it).toDouble() }.roundToInt(),
					relationNext.keys.averageBy { getZ(it).toDouble() }.roundToInt()
				)

				return previousVec.distance(nextVec).roundToInt()
			}

			// Rough distance
			return getCenter().distance(previousNode.getCenter()).roundToInt()
		}

		// As a fallback, shouldn't happen
		return 0
	}
}
