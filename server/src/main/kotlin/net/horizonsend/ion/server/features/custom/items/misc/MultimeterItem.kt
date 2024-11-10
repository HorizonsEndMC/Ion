package net.horizonsend.ion.server.features.custom.items.misc

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerPathfindingNode
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.features.transport.node.util.PathfindingNodeWrapper
import net.horizonsend.ion.server.features.transport.node.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.node.util.getHeuristic
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_VARIANT
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.X
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.Z
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.LONG
import java.util.PriorityQueue

object MultimeterItem : CustomItem("Multimeter") {
	override fun constructItemStack(): ItemStack {
		return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
			it.displayName(Component.text("Multimeter", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
		}
	}

	override fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {
		val targeted = livingEntity.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updateMeta {
			it.persistentDataContainer.set(X, LONG, key)
		}

		livingEntity.information("Set first point to ${toVec3i(key)}")

		tryCheckResistance(livingEntity, livingEntity.world, itemStack)
	}

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (livingEntity !is Player) return
		if (livingEntity.isSneaking) {
			cycleNetworks(livingEntity, livingEntity.world, itemStack)
			return
		}

		val targeted = livingEntity.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updateMeta {
			it.persistentDataContainer.set(Z, LONG, key)
		}

		livingEntity.information("Set second point to ${toVec3i(key)}")

		tryCheckResistance(livingEntity, livingEntity.world, itemStack)
	}

	private fun tryCheckResistance(audience: Audience, world: World, itemStack: ItemStack) {
		val firstPoint = itemStack.itemMeta.persistentDataContainer.get(X, LONG) ?: return
		val firstChunk = IonChunk[world, getX(firstPoint).shr(4), getZ(firstPoint).shr(4)] ?: return
		val secondPoint = itemStack.itemMeta.persistentDataContainer.get(Z, LONG) ?: return
		val secondChunk = IonChunk[world, getX(secondPoint).shr(4), getZ(secondPoint).shr(4)] ?: return

		val networkTypeIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_VARIANT, INTEGER, 0)
		val networkType = NetworkType.entries[networkTypeIndex]

		val firstNode = networkType.get(firstChunk).getOrCache(firstPoint) ?: return audience.information("There is no node at ${toVec3i(firstPoint)}")
		val secondNode = networkType.get(secondChunk).getOrCache(secondPoint)  ?: return audience.information("There is no node at ${toVec3i(secondPoint)}")

		val path = getIdealPath(audience, firstNode, secondNode)
		val resistance = calculatePathResistance(path) ?: return audience.userError("There is no path connecting these nodes")
		audience.information("The resistance from ${firstNode.javaClass.simpleName} at ${toVec3i(firstPoint)} to ${secondNode.javaClass.simpleName} at ${toVec3i(secondPoint)} is $resistance")
	}

	private fun cycleNetworks(audience: Audience, world: World, itemStack: ItemStack) {
		val currentIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_VARIANT, INTEGER, 0)
		val newIndex = (currentIndex + 1) % NetworkType.entries.size
		itemStack.updateMeta {
			it.persistentDataContainer.set(NODE_VARIANT, INTEGER, newIndex)
		}

		audience.success("Set network type to ${NetworkType.entries[newIndex]}")

		tryCheckResistance(audience, world, itemStack)
	}

	/**
	 * Uses the A* algorithm to find the shortest available path between these two nodes.
	 **/
	private fun getIdealPath(audience: Audience, fromPos: BlockKey, toPos: BlockKey): Array<TransportNode>? {
		// There are 2 collections here. First the priority queue contains the next nodes, which needs to be quick to iterate.
		val queue = PriorityQueue<PathfindingNodeWrapper> { o1, o2 -> o2.f.compareTo(o1.f) }
		// The hash set here is to speed up the .contains() check further down the road, which is slow with the queue.
		val queueSet = IntOpenHashSet()

		fun queueAdd(wrapper: PathfindingNodeWrapper) {
			queue.add(wrapper)
			queueSet.add(wrapper.node.hashCode())
		}

		fun queueRemove(wrapper: PathfindingNodeWrapper) {
			queue.remove(wrapper)
			queueSet.remove(wrapper.node.hashCode())
		}

		queueAdd(PathfindingNodeWrapper(from, null, 0, 0))

		val visited = IntOpenHashSet()

		// Safeguard
		var iterations = 0L

		while (queue.isNotEmpty() && iterations < 150) {
			iterations++
			val current = queue.minBy { it.f }
			Tasks.syncDelay(iterations) { audience.highlightBlock(current.node.getCenter(), 5L) }
			audience.information("current: ${current.node.javaClass.simpleName} at ${current.node.getCenter()}")
			if (current.node == to) return current.buildPath()

			queueRemove(current)
			visited.add(current.node.hashCode())

			val neighbors = getNeighborsB(audience, current, to)
			if (neighbors.isEmpty()) audience.userError("Empty neighbors")

			for (newNeighbor in neighbors) {
				audience.information("new neighbor: ${newNeighbor.node} at ${newNeighbor.node.getCenter()}")
				if (visited.contains(newNeighbor.node.hashCode())) {
					audience.information("conmtinue")
					continue
				}

				newNeighbor.f = (newNeighbor.g + getHeuristic(newNeighbor, to))

				if (queueSet.contains(newNeighbor.node.hashCode())) {
					audience.information("Existing in queue")
					val existingNeighbor = queue.first { it.node === newNeighbor.node }
					if (newNeighbor.g < existingNeighbor.g) {
						existingNeighbor.g = newNeighbor.g
						existingNeighbor.f = newNeighbor.f
						existingNeighbor.parent = newNeighbor.parent
					}
				} else {
					audience.information("Adding to queue")
					queueAdd(newNeighbor)
				}
			}
		}

		audience.userError("Exhausted queue, $iterations")

		return null
	}

	// Wraps neighbor nodes in a data class to store G and F values for pathfinding. Should probably find a better solution
	private fun getNeighborsB(audience: Audience, parent: PathfindingNodeWrapper, destination: TransportNode): Array<PathfindingNodeWrapper> {
		val parentParent = parent.parent
		audience.information("Parent: ${parent.node.getTransferableNodes()}")

		val transferable = if (parentParent != null && parent.node is PowerPathfindingNode) {
			parent.node.getNextNodes(parentParent.node, destination)
		} else parent.node.cachedTransferable

		audience.information("New transferable: ${transferable.joinToString { it.javaClass.simpleName }}")

		return Array(transferable.size) {
			val neighbor = transferable[it]

			PathfindingNodeWrapper(
				node = neighbor,
				parent = parent,
				g = parent.g + parent.node.getDistance(neighbor).toInt(),
				f = 1
			)
		}
	}
}
