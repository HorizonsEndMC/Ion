package net.horizonsend.ion.server.features.custom.items.misc

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
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
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.LONG
import java.util.PriorityQueue

object MultimeterItem : CustomItem("MULTIMETER", Component.text("Multimeter", NamedTextColor.YELLOW), ItemFactory.unStackableCustomItem) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@MultimeterItem) { event, _, itemStack ->
			if (event.player.isSneaking) {
				cycleNetworks(event.player, event.player.world, itemStack)

				return@rightClickListener
			}

			setSecondPoint(event.player, itemStack)
		})

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@MultimeterItem) { event, _, itemStack ->
			setFirstPoint(event.player, itemStack)
		})
	}

	private fun setFirstPoint(player: Player, itemStack: ItemStack) {
		val targeted = player.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updatePersistentDataContainer { set(X, LONG, key) }

		player.information("Set first point to ${toVec3i(key)}")

		tryCheckResistance(player, player.world, itemStack)
	}

	private fun setSecondPoint(player: Player, itemStack: ItemStack) {
		val targeted = player.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		itemStack.updatePersistentDataContainer { set(Z, LONG, key) }

		player.information("Set second point to ${toVec3i(key)}")

		tryCheckResistance(player, player.world, itemStack)
	}

	private fun tryCheckResistance(audience: Audience, world: World, itemStack: ItemStack) {
		val firstPoint = itemStack.itemMeta.persistentDataContainer.get(X, LONG) ?: return
		val firstChunk = IonChunk[world, getX(firstPoint).shr(4), getZ(firstPoint).shr(4)] ?: return
		val secondPoint = itemStack.itemMeta.persistentDataContainer.get(Z, LONG) ?: return

		val networkTypeIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_TYPE, INTEGER, 0)
		val cacheType = CacheType.entries[networkTypeIndex]

		val firstNode = cacheType.get(firstChunk).getOrCache(firstPoint)
		if (firstNode == null) {
			audience.information("There is no node at ${toVec3i(firstPoint)}")
			return
		}

		val secondNode = cacheType.get(firstChunk).getOrCache(secondPoint)
		if (secondNode == null) {
			audience.information("There is no node at ${toVec3i(secondPoint)}")
			return
		}

		val path = getIdealPath(
			audience = audience,
			fromNode = Node.NodePositionData(firstNode, world, firstPoint, BlockFace.SELF, cacheType.get(firstChunk)),
			destination = secondPoint
		) ?: return

		val resistance = calculatePathResistance(path)
		audience.information("The resistance from ${firstNode.javaClass.simpleName} at ${toVec3i(firstPoint)} to ${secondNode.javaClass.simpleName} at ${toVec3i(secondPoint)} is $resistance")
	}

	private fun cycleNetworks(audience: Audience, world: World, itemStack: ItemStack) {
		val currentIndex = itemStack.itemMeta.persistentDataContainer.getOrDefault(NODE_TYPE, INTEGER, 0)
		val newIndex = (currentIndex + 1) % CacheType.entries.size

		itemStack.updatePersistentDataContainer { set(NODE_TYPE, INTEGER, newIndex) }

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
			queue.add(wrapper)
			queueSet.add(wrapper.node.position)
		}

		fun queueRemove(wrapper: PathfindingNodeWrapper) {
			queue.remove(wrapper)
			queueSet.remove(wrapper.node.position)
		}

		queueAdd(PathfindingNodeWrapper(
			node = fromNode,
			parent = null,
			g = 0,
			f = getHeuristic(fromNode, destination)
		))

		val visited = Long2IntOpenHashMap()

		fun markVisited(node: PathfindingNodeWrapper) {
			val pos = node.node.position
			val existing = visited.getOrDefault(pos, 0)

			visited[pos] = existing + 1
		}

		fun canVisit(node: Node.NodePositionData): Boolean {
			return visited.getOrDefault(node.position, 0) < node.type.getMaxPathfinds()
		}

		// Safeguard
		var iterations = 0L

		val maxDepth = ConfigurationFiles.transportSettings().powerConfiguration.maxPathfindDepth
		while (queue.isNotEmpty() && iterations < maxDepth) {
			iterations++
			val current = queue.minBy { it.f }

			Tasks.syncDelay(iterations) { audience.highlightBlock(toVec3i(current.node.position), 5L) }

			if (current.node.position == destination) {
				val path = current.buildPath()

				path.forEach { location -> audience.highlightBlock(toVec3i(location.position), 50L) }

				return path
			}

			queueRemove(current)
			markVisited(current)

			val neighbors = getNeighbors(
				current,
				{ nodeCache, cacheType, world, pos -> getOrCacheNode(nodeCache, cacheType, world, pos) },
				null
			)

			for (computedNeighbor in neighbors) {
				if (!canVisit(computedNeighbor.node)) {
					continue
				}

				computedNeighbor.f = (computedNeighbor.g + getHeuristic(computedNeighbor.node, destination))

				if (queueSet.contains(computedNeighbor.node.position)) {
					val existingNeighbor = queue.first { it.node.position == computedNeighbor.node.position }

					if (computedNeighbor.g < existingNeighbor.g) {
						existingNeighbor.parent = computedNeighbor.parent

						existingNeighbor.g = computedNeighbor.g
						existingNeighbor.f = computedNeighbor.f
					}
				} else {
					queueAdd(computedNeighbor)
				}
			}
		}

		audience.userError("Exhausted queue, after $iterations, could not find destination.")

		return null
	}
}
