package net.horizonsend.ion.server.features.custom.items.misc

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.PathfindingNodeWrapper
import net.horizonsend.ion.server.features.transport.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.util.getHeuristic
import net.horizonsend.ion.server.features.transport.util.getNeighbors
import net.horizonsend.ion.server.features.transport.util.getOrCacheNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_TYPE
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
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.LONG
import java.util.PriorityQueue

object MultimeterItem : CustomItem("MULTIMETER", Component.text("Multimeter", NamedTextColor.YELLOW), ItemFactory.unStackableCustomItem) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@MultimeterItem) { event, _, itemStack ->
			handleSecondaryInteract(event.player, itemStack)
		})
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@MultimeterItem) { event, _, itemStack ->
			handlePrimaryInteract(event.player, itemStack)
		})
	}

	private fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		val targeted = livingEntity.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updateMeta {
			it.persistentDataContainer.set(X, LONG, key)
		}

		livingEntity.information("Set first point to ${toVec3i(key)}")

		tryCheckResistance(livingEntity, livingEntity.world, itemStack)
	}

	private fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
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

		val networkTypeIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_TYPE, INTEGER, 0)
		val cacheType = CacheType.entries[networkTypeIndex]

		val firstNode = cacheType.get(firstChunk).getOrCache(firstPoint) ?: return audience.information("There is no node at ${toVec3i(firstPoint)}")

		val path = getIdealPath(audience, Node.NodePositionData(firstNode, world, firstPoint, BlockFace.SELF), secondPoint) ?: return audience.userError("There is no path connecting these nodes")
		val resistance = calculatePathResistance(path)
		audience.information("The resistance from ${firstNode.javaClass.simpleName} at ${toVec3i(firstPoint)} to ${toVec3i(secondPoint)} at ${toVec3i(secondPoint)} is $resistance")
	}

	private fun cycleNetworks(audience: Audience, world: World, itemStack: ItemStack) {
		val currentIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_TYPE, INTEGER, 0)
		val newIndex = (currentIndex + 1) % CacheType.entries.size
		itemStack.updateMeta {
			it.persistentDataContainer.set(NODE_TYPE, INTEGER, newIndex)
		}

		audience.success("Set network type to ${CacheType.entries[newIndex]}")

		tryCheckResistance(audience, world, itemStack)
	}

	/**
	 * Uses the A* algorithm to find the shortest available path between these two nodes.
	 **/
	private fun getIdealPath(audience: Audience, fromNode: Node.NodePositionData, destination: BlockKey): Array<Node.NodePositionData>? {
		// There are 2 collections here. First the priority queue contains the next nodes, which needs to be quick to iterate.
		val queue = PriorityQueue<PathfindingNodeWrapper> { o1, o2 -> o2.f.compareTo(o1.f) }
		// The hash set here is to speed up the .contains() check further down the road, which is slow with the queue.
		val queueSet = LongOpenHashSet()

		fun queueAdd(wrapper: PathfindingNodeWrapper) {
			audience.information("adding $wrapper")
			queue.add(wrapper)
			queueSet.add(wrapper.node.position)
		}

		fun queueRemove(wrapper: PathfindingNodeWrapper) {
			audience.information("removing $wrapper")
			queue.remove(wrapper)
			queueSet.remove(wrapper.node.position)
		}

		queueAdd(PathfindingNodeWrapper(
			node = fromNode,
			parent = null,
			g = 0,
			f = getHeuristic(fromNode, destination)
		))

		val visited = LongOpenHashSet()

		// Safeguard
		var iterations = 0L

		val maxDepth = ConfigurationFiles.transportSettings().powerConfiguration.maxPathfindDepth
		while (queue.isNotEmpty() && iterations < maxDepth) {
			iterations++
			val current = queue.minBy { it.f }
			Tasks.syncDelay(iterations) { audience.highlightBlock(toVec3i(current.node.position), 5L) }
			audience.information("current: ${current.node.javaClass.simpleName} at ${toVec3i(current.node.position)}")
			if (current.node.position == destination) return current.buildPath()

			queueRemove(current)
			visited.add(current.node.position)

			val neighbors = getNeighbors(current, { cacheType, world, pos -> getOrCacheNode(cacheType, world, pos) }, null)
			audience.userError("Found ${neighbors.size} neighbors")

			for (newNeighbor in neighbors) {
				audience.information("new neighbor: $newNeighbor at ${toVec3i(newNeighbor.node.position)}")
				if (visited.contains(newNeighbor.node.position)) {
					audience.information("conmtinue")
					continue
				}

				newNeighbor.f = (newNeighbor.g + getHeuristic(newNeighbor.node, destination))

				if (queueSet.contains(newNeighbor.node.position)) {
					audience.information("Neighbor exists in queue")
					val existingNeighbor = queue.first { it.node.position == newNeighbor.node.position }

					if (newNeighbor.g < existingNeighbor.g) {
						audience.information("New path is ideal, updating neighbor to match")
						existingNeighbor.parent = newNeighbor.parent

						existingNeighbor.g = newNeighbor.g
						existingNeighbor.f = newNeighbor.f
					}
				} else {
					audience.information("Not present, Adding to queue")
					queueAdd(newNeighbor)
				}
			}
		}

		audience.userError("Exhausted queue, $iterations")

		return null
	}
}
