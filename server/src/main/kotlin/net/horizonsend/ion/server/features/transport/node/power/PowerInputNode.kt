package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.network.holders.ChunkNetworkHolder
import net.horizonsend.ion.server.features.transport.network.holders.ShipNetworkHolder
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerInputNode(override val network: PowerNetwork) : SingleNode {
	constructor(network: PowerNetwork, position: BlockKey) : this(network) {
		this.position = position
	}

	override var isDead: Boolean = false
	override var position by Delegates.notNull<Long>()
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node is PowerExtractorNode
	}

	override suspend fun buildRelations(position: BlockKey) {
		super.buildRelations(position)
	}

	fun getPoweredMultiblocks(): Iterable<PoweredMultiblockEntity> {
		val x = getX(position)
		val y = getY(position)
		val z = getZ(position)

		return offsets.mapNotNull { offset ->
			val newX = x + offset.x
			val newY = y + offset.y
			val newZ = z + offset.z

			val newKey = toBlockKey(newX, newY, newZ)

			return@mapNotNull when (val holder = network.holder) {
				is ChunkNetworkHolder<*> -> {
					holder.manager.chunk.multiblockManager[newKey] as? PoweredMultiblockEntity
				}
				is ShipNetworkHolder<*> -> {
					null //TODO
				}
				else -> null
			}
		}
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	companion object {
		private val offsets = setOf(
			// most multiblocks have the sign a block up and out of the computer
			Vec3i(1, 1, 0), Vec3i(-1, 1, 0), Vec3i(0, 1, -1), Vec3i(0, 1, 1),
			// power cells have it on the block
			Vec3i(1, 0, 0), Vec3i(-1, 0, 0), Vec3i(0, 0, -1), Vec3i(0, 0, 1),
			// drills have it on a corner
			Vec3i(-1, 0, -1), Vec3i(1, 0, -1), Vec3i(1, 0, 1), Vec3i(-1, 0, 1),
			// upside down mining lasers have signs below
			Vec3i(1, -1, 0), Vec3i(-1, -1, 0), Vec3i(0, -1, -1), Vec3i(0, -1, 1),
			// up and down
			Vec3i(0, 1, 0), Vec3i(0, -1, 0)
		)
	}

	override fun toString(): String = "POWER INPUT NODE: ${getPoweredMultiblocks().toList().size} powered multiblocks, Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes"
}
