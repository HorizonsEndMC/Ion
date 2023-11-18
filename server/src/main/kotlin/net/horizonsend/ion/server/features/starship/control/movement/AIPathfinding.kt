package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.AStarPathfindingEngine
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.highlightRegion
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.LevelChunkSection
import net.minecraft.world.level.chunk.PalettedContainer
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs

/*
 * Pathfinding:
 *
 * Uses level chunk sections as nodes, if filled, they are marked as unnavigable
 * Considers only non-ship blocks
 */
object AIPathfinding {
	data class SectionNode(val world: World, val position: Vec3i, val navigable: Boolean) {
		val center: Vec3i = Vec3i(
			position.x.shl(4) + 8,
			position.y.shl(4) + world.minHeight + 8,
			position.z.shl(4) + 8
		)

		fun anyNeighborsUnnavigable(trackedNodes: ConcurrentLinkedQueue<SectionNode>): Boolean =
			getNeighbors(trackedNodes).any { neighborNeighbor -> !neighborNeighbor.value.navigable }

		/** G cost, the sum of the distances between the origin and current node */
		private fun getGCost(relation: Adjacent, parent: PathfindingNodeWrapper): Float {
			return parent.getChain().sumOf { it.parentRelation?.cost?.toInt() ?: 0 }.toFloat() + relation.cost
		}

		/** Heuristic, the distance from the destination */
		private fun getHCost(destinationNode: SectionNode): Float {
			return getEstimatedCostManhattan(this, destinationNode)
		}

		fun getFCost(parentRelation: Adjacent, destinationNode: SectionNode, allNodes: ConcurrentLinkedQueue<SectionNode>, parentWrapper: PathfindingNodeWrapper): Float {
			val gCost = getGCost(parentRelation, parentWrapper)
			val hCost = getHCost(destinationNode)
			val additional = getAdditionalCost(allNodes)

//			debugAudience.debug("""
//				parent F cost: $accumulated
//				gCost: $gCost
//				gCostEuclidean: ${getEstimatedCostEuclidean(this, originNode)}
//				gCostEuclidean: ${getEstimatedCostManhattan(this, originNode)}
//
//				hCost: $hCost
//				hCostEuclidean: ${getEstimatedCostEuclidean(this, destinationNode)}
//				hCostEuclidean: ${getEstimatedCostManhattan(this, destinationNode)}
//
//				additionalCost: $additional
//			""".trimIndent())

			return gCost + hCost + additional
		}

		fun getAdditionalCost(trackedNodes: ConcurrentLinkedQueue<SectionNode>): Float {
			var additional = 0f

			if (anyNeighborsUnnavigable(trackedNodes)) additional += 25.0f

			return additional
		}

		fun getNeighbors(trackedNodes: ConcurrentLinkedQueue<SectionNode>): Map<Adjacent, SectionNode> {
			val (x, y, z) = position
			val sections = mutableMapOf<Adjacent, SectionNode>()

			for (face in Adjacent.values()) {
				val newX = x + face.modX
				val newY = y + face.modY
				val newZ = z + face.modZ

				val position = Vec3i(newX, newY, newZ)
				val sectionNode = get(trackedNodes, position) ?: continue

				sections[face] = sectionNode
			}

			return sections
		}

		fun highlight(player: Player, duration: Long = 200L) {
			val (positionX, positionY, positionZ) = position

			val minPointX = positionX.shl(4)
			val minPointY = positionY.shl(4)
			val minPointZ = positionZ.shl(4)

			val maxPointX = minPointX + 16
			val maxPointY = minPointY + 16
			val maxPointZ = minPointZ + 16

			player.highlightRegion(Vec3i(minPointX, minPointY, minPointZ), Vec3i(maxPointX, maxPointY, maxPointZ), "", duration)
		}

		// Will throw an exception if the nodes are not immediate neighbors.
		fun getRelation(from: SectionNode): Adjacent {
			val offset = position - from.position

			return Adjacent.values().first { it.offset == offset }
		}

		companion object {
			@Synchronized
			fun get(trackedNodes: ConcurrentLinkedQueue<SectionNode>, position: Vec3i): SectionNode? {
				return trackedNodes.firstOrNull { it.position == position }
			}
		}
	}

	/** Searches the chunk for being navigable */
	@Synchronized
	fun checkNavigability(searchingStarship: ActiveStarship?, world: World, chunk: LevelChunk, yRange: Iterable<Int>): Set<SectionNode> {
		val sectionNodes = mutableSetOf<SectionNode>()
		val levelChunkSections: Array<out LevelChunkSection> = chunk.sections

		val chunkX = chunk.pos.x
		val chunkZ = chunk.pos.z

		if (!chunk.level.worldBorder.isChunkInBounds(chunkX, chunkZ)) {
			return yRange.map { SectionNode(world, Vec3i(chunkX, it, chunkZ), false) }.toSet()
		}

		for (sectionPos in yRange) {
			val section = levelChunkSections.getOrNull(sectionPos)
			val pos = Vec3i(chunkX, sectionPos, chunkZ)

			// Section is not inside the build limit, and is not navigable
			if (section == null) {
				sectionNodes += SectionNode(world, pos, false)
				continue
			}

			// Section is only air, navigable
			if (section.hasOnlyAir()) {
				sectionNodes += SectionNode(world, pos, true)
				continue
			}

			if (searchingStarship != null && sectionContainsOnlySelfBlocks(searchingStarship, section, world, pos)) {
				sectionNodes += SectionNode(world, pos, true)
				continue
			}

			// Section has blocks, not navigable
			sectionNodes += SectionNode(world, pos, false)
		}

		return sectionNodes
	}

	/** Assumes that the section has already been checked to not be empty */
	@Synchronized
	private fun sectionContainsOnlySelfBlocks(starship: ActiveStarship, section: LevelChunkSection, world: World, sectionPosition: Vec3i): Boolean {
		val (sectionX, sectionY, sectionZ) = sectionPosition
		val originX = sectionX.shl(4)
		val originY = sectionY.shl(4) + world.minHeight
		val originZ = sectionZ.shl(4)

		val strategy = PalettedContainer.Strategy.SECTION_STATES
		val states = section.states

		for (x in 0..15) {
			val realX = originX + x

			for (y in 0..15) {
				val realY = originY + y

				for (z in 0..15) {
					val realZ = originZ + z
					val index = strategy.getIndex(x, y, z)

					if (states[index].isAir) continue

					// If it's not air, and it's not part of the ships, then it contains non-ship blocks
					if (!starship.contains(realX, realY, realZ)) return false
				}
			}
		}

		return true
	}

	/** Takes a list of sections across multiple chunks, and returns those sections being searched */
	@Synchronized
	fun searchSections(starship: ActiveStarship?, world: World, sections: Collection<Vec3i>, loadChunks: Boolean = false): Set<SectionNode> {
		val chunkMap = sections.groupBy { ChunkPos(it.x, it.z) }
		val nmsWorld = world.minecraft

		val sectionNodes = mutableSetOf<SectionNode>()

		for ((chunkPos, sectionsList) in chunkMap) {
			val (x, z) = chunkPos

			val chunk = nmsWorld.getChunkIfLoaded(x, z) ?: if (loadChunks) nmsWorld.getChunk(x, z) else null
				?: continue

			val nodes = checkNavigability(starship, world, chunk, sectionsList.map { it.y })

			sectionNodes.addAll(nodes)
		}

		return sectionNodes
	}

	/** Gets the chunks that should be searched for pathfinding */
	@Synchronized
	private fun getSurroundingSectionPositions(engine: AStarPathfindingEngine): List<Vec3i> {
		val radius = engine.chunkSearchRadius
		val center = engine.getSectionPositionOrigin()

		val centerChunkX = center.x
		val centerSectionY = center.y
		val centerChunkZ = center.z

		val xRange = (-radius + centerChunkX)..(+radius + centerChunkX)
		val yRange = (-radius + centerSectionY)..(+radius + centerSectionY)
		val zRange = (-radius + centerChunkZ)..(+radius + centerChunkZ)

		val positions = mutableListOf<Vec3i>()

		for (x in xRange) for (z in zRange) for (y in yRange) {
			positions += Vec3i(x, y, z)
		}

		return positions
	}

	/**
	 * Adjusts the tracked sections when the AI has moved
	 * Saves time by not searching existing sections, and maintains inhabited time of currently tracked sections
	 **/
	@Synchronized
	fun adjustTrackedSections(engine: AStarPathfindingEngine, loadChunks: Boolean = false) {
		val currentlyTracked = engine.trackedSections.map { it.position }
		val new = getSurroundingSectionPositions(engine)

		val toRemove = currentlyTracked.toMutableList() // clone
		toRemove.removeAll(new) // Get a list of all the old

		val unTracked = new.toMutableList()
		val newSections = searchSections(engine.starship, engine.world, unTracked, loadChunks)

		engine.trackedSections.removeAll { toRemove.contains(it.position) }
		engine.trackedSections.addAll(newSections)
	}

	/**
	 * A* Algorithm.
	 *
	 * TODO insert explainer of how A* works
	 **/
	@Synchronized
	fun pathfind(engine: AStarPathfindingEngine): Collection<PathfindingNodeWrapper> {
		val trackedNodes = engine.trackedSections
		if (trackedNodes.isEmpty()) adjustTrackedSections(engine, false)

		val originNode = engine.getOriginNode()
		val destinationNode = engine.getDestinationNode()

		// All nodes available for pathfinding
		val navigableList = ConcurrentLinkedQueue<SectionNode>()
		trackedNodes.filterTo(navigableList) { it.navigable }

		// Open nodes is a list of all nodes that have not been evaluated
		val openSet: MutableSet<PathfindingNodeWrapper> = mutableSetOf()

		// Closed nodes is a list of nodes that have been evaluated. Every one has a parent.
		val closedSet: MutableSet<PathfindingNodeWrapper> = mutableSetOf()

		// The current node, may be part of the path, may not.
		val originNodeWrapper = PathfindingNodeWrapper(originNode, 0f, false, null, null)
		openSet += originNodeWrapper

		engine.starship.debug("Beginning A*, Origin: $originNode, destination: $destinationNode")
		iterateNeighbors(originNodeWrapper, navigableList, trackedNodes, openSet, closedSet, destinationNode, originNode)

		var iterations = 0
		while (iterations <= 1000) {
			iterations++

			val currentNode = openSet.minBy { it.fCost }

			openSet.remove(currentNode)
			closedSet += currentNode

			if (currentNode.node.position == destinationNode.position) {
				return currentNode.getChain()
			}

			iterateNeighbors(currentNode, navigableList, trackedNodes, openSet, closedSet, destinationNode, originNode)
		}

		engine.starship.debug("COULD NOT FIND DESTINATION, GAVE UP")
		return openSet.minBy { it.fCost }.getChain()
	}

	@Synchronized
	fun iterateNeighbors(
		currentNode: PathfindingNodeWrapper,
		navigableNodes: ConcurrentLinkedQueue<SectionNode>,
		allNodes: ConcurrentLinkedQueue<SectionNode>,
		openSet: MutableSet<PathfindingNodeWrapper>,
		closedSet: MutableSet<PathfindingNodeWrapper>,
		destinationNode: SectionNode,
		originNode: SectionNode
	) {
		val neighbors = currentNode.node.getNeighbors(navigableNodes)

		for ((relation: Adjacent, neighborNode: SectionNode) in neighbors) {
			if (closedSet.any { it.node == neighborNode }) {
				continue
			}

			var shouldContinue = false

			// Handle an existing node
			openSet.firstOrNull { it.node.position == neighborNode.position }?.let { existingNeighbor: PathfindingNodeWrapper ->
				// Should not add it to the list now
				shouldContinue = true

				val newFCost = neighborNode.getFCost(relation, destinationNode, allNodes, currentNode)

				// If the new F cost is lower, set its parent to this node, and the new, lower, F cost
				if (newFCost < existingNeighbor.fCost) {
					existingNeighbor.parent = currentNode
					existingNeighbor.fCost = newFCost
				}
			}

			if (shouldContinue) continue

			val wrapper = PathfindingNodeWrapper(
				neighborNode,
				neighborNode.getFCost(relation, destinationNode, allNodes, currentNode),
				false,
				relation,
				currentNode
			)

			openSet += wrapper
		}
	}

	/**
	 * Wraps a pathfinding node to provide the information gained from the algorithm
	 *
	 * @param node: The node this wraps
	 * @param parent: The parent of this node, used to calculate G cost.
	 * @param parent: The positional relation to the parent.
	 **/
	data class PathfindingNodeWrapper(
		val node: SectionNode,
		var fCost: Float,
		val origin: Boolean = false,
		val parentRelation: Adjacent?,
		var parent: PathfindingNodeWrapper?,
	) {
		/** Iterates backwards from this node, through its parents, to the origin. */
		fun iterateParents(b: (PathfindingNodeWrapper) -> Unit) {
			getChain().forEach(b)
		}

		/** Collects a list of pathfinding node wrappers */
		fun getChain(): Collection<PathfindingNodeWrapper> {
			var current: PathfindingNodeWrapper? = this
			val path = mutableListOf<PathfindingNodeWrapper>()

			while (current != null) {
				// If it is the origin node, break
				if (current.origin) break

				// shouldn't happen since it's not the origin, but handle the possibility
				current = current.parent ?: break

				path.add(current)
			}

			return path
		}

		companion object {
			fun get(closedNodes: Collection<PathfindingNodeWrapper>, sectionNode: SectionNode): PathfindingNodeWrapper? {
				return closedNodes.firstOrNull { it.node == sectionNode }
			}
		}
	}

	// Use squares for estimation
	private fun getEstimatedCostEuclidean(node: SectionNode, destinationNode: SectionNode): Float =
		distanceSquared(node.position, destinationNode.position).toFloat()

	fun getEstimatedCostManhattan(node: SectionNode, destinationNode: SectionNode): Float {
		val origin = node.position

		val (x, y, z) = destinationNode.position

		val xDiff = abs(origin.x - x) * 16.0f
		val yDiff = abs(origin.y - y) * 16.0f
		val zDiff = abs(origin.z - z) * 16.0f

		return xDiff + yDiff + zDiff
	}

	class PathfindingException(message: String) : Exception(message)

	enum class Adjacent(val modX: Int, val modY: Int, val modZ: Int, val cost: Float) {
		NORTH(0, 0, -1, 16.0f),
		UP_NORTH(0, 1, -1, 22.6f),
		DOWN_NORTH(0, -1, -1, 22.6f),
		NORTH_EAST(1, 0, -1, 22.6f),
		NORTH_WEST(-1, 0, -1, 22.6f),
		UP_NORTH_EAST(1, 1, -1, 27.7f),
		UP_NORTH_WEST(-1, 1, -1, 27.7f),
		DOWN_NORTH_EAST(1, -1, -1, 27.7f),
		DOWN_NORTH_WEST(-1, -1, -1, 27.7f),
		EAST(1, 0, 0, 16.0f),
		UP_EAST(1, 1, 0, 22.6f),
		DOWN_EAST(1, -1, 0, 22.6f),
		SOUTH(0, 0, 1, 16.0f),
		UP_SOUTH(0, 1, 1, 22.6f),
		DOWN_SOUTH(0, -1, 1, 22.6f),
		SOUTH_EAST(1, 0, 1, 22.6f),
		SOUTH_WEST(-1, 0, 1, 22.6f),
		UP_SOUTH_EAST(1, 1, 1, 27.7f),
		UP_SOUTH_WEST(-1, 1, 1, 27.7f),
		DOWN_SOUTH_EAST(1, -1, 1, 27.7f),
		DOWN_SOUTH_WEST(-1, -1, 1, 27.7f),
		WEST(-1, 0, 0, 16.0f),
		UP_WEST(-1, 1, 0, 22.6f),
		DOWN_WEST(-1, -1, 0, 22.6f),
		UP(0, 1, 0, 16.0f),
		DOWN(0, -1, 0, 16.0f),

		;

		val offset: Vec3i = Vec3i(modX, modY, modZ)
	}
}
