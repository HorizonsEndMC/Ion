package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
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
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import kotlin.math.abs

/*
 * Pathfinding:
 *
 * Uses level chunk sections as nodes, if filled, they are marked as unnavigable
 * Considers only non-ship blocks
 */
object AIPathfinding {
	const val MAX_A_STAR_ITERATIONS = 20

	data class SectionNode(val world: World, val position: Vec3i, val navigable: Boolean) {
		var inhabitedTime = 0

		val center: Vec3i = Vec3i(
			position.x.shl(4) + 8,
			position.y.shl(4) + world.minHeight + 8,
			position.z.shl(4) + 8
		)
		fun anyNeighborsUnnavigable(trackedNodes: Collection<SectionNode>): Boolean =
			getNeighborNodes(this, trackedNodes).any { neighborNeighbor -> !neighborNeighbor.navigable }

		fun highlight(player: Player) {
			val (positionX, positionY, positionZ) = position

			val minPointX = positionX.shl(4)
			val minPointY = positionY.shl(4)
			val minPointZ = positionZ.shl(4)

			val maxPointX = minPointX + 16
			val maxPointY = minPointY + 16
			val maxPointZ = minPointZ + 16

			player.highlightRegion(Vec3i(minPointX, minPointY, minPointZ), Vec3i(maxPointX, maxPointY, maxPointZ))
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
	private fun getSurroundingSectionPositions(engine: PathfindingEngine): List<Vec3i> {
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
	fun adjustTrackedSections(engine: PathfindingEngine, loadChunks: Boolean = false) {
		val currentlyTracked = engine.trackedSections.map { it.position }
		val new = getSurroundingSectionPositions(engine)

		val toRemove = currentlyTracked.toMutableList() // clone
		toRemove.removeAll(new) // Get a list of all the old

		val unTracked = new.toMutableList()
		val newSections = searchSections(engine.starship, engine.world, unTracked, loadChunks)

		engine.trackedSections.removeAll { toRemove.contains(it.position) }
		engine.trackedSections.addAll(newSections)
	}

	fun pathfind(engine: PathfindingEngine) {
		val trackedNodes = engine.trackedSections
		if (trackedNodes.isEmpty()) adjustTrackedSections(engine, false)

		val originNode = engine.getOriginNode()

		val openList: List<SectionNode> = listOf()
		val closedList: List<SectionNode> = listOf()


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
		var parent: SectionNode?,
		val parentRelation: Adjacent?,
		val origin: Boolean = false
	) {
		/** G cost, the sum of the distances between the origin and current node */
		fun getGCost(closedNodes: Collection<PathfindingNodeWrapper>): Int {
			val chain = getChain(closedNodes)

			return chain.sumOf { it.parentRelation?.cost ?: 0 }
		}

		/** Heuristic, the distance from the destination */
		fun getHCost(destinationNode: SectionNode): Int {
			return getEstimatedCostEuclidean(this.node, destinationNode)
		}

		fun getFCost(closedNodes: Collection<PathfindingNodeWrapper>, destinationNode: SectionNode): Int {
			return  getGCost(closedNodes) + getHCost(destinationNode)
		}

		/** Iterates backwards from this node, through its parents, to the origin. */
		fun iterateParents(closedNodes: Collection<PathfindingNodeWrapper>, b: (PathfindingNodeWrapper) -> Unit) {
			getChain(closedNodes).forEach(b)
		}

		/** Collects a list of pathfinding node wrappers */
		fun getChain(closedNodes: Collection<PathfindingNodeWrapper>): Collection<PathfindingNodeWrapper> {
			var current: SectionNode? = this.node
			val path = mutableListOf<PathfindingNodeWrapper>()

			while (current != null) {
				val pathfinding = get(closedNodes, current) ?: break
				// If it is the origin node, break
				if (pathfinding.origin) break

				// shouldn't happen since it's not the origin, but handle the possibility
				current = pathfinding.parent ?: break

				path.add(pathfinding)
			}

			return path
		}

		companion object {
			fun get(closedNodes: Collection<PathfindingNodeWrapper>, sectionNode: SectionNode): PathfindingNodeWrapper? {
				return closedNodes.firstOrNull { it.node == sectionNode }
			}
		}
	}

	// Begin A* Implementation
	/** Nodes must be populated first */
	@Synchronized
	fun findNavigationNodes(
		engine: PathfindingEngine,
		destination: Vec3i,
		previousPositions: Collection<SectionNode>
	): List<SectionNode> {
		val searchDistance = engine.chunkSearchRadius

		val allNodes = engine.trackedSections.toSet()

		val currentPos = engine.getCenter()
		val originNodeLocation = engine.getSectionPositionOrigin()

		val destinationNodeLocation = getDestinationNode(currentPos, destination, searchDistance)
		val destinationNode = allNodes.firstOrNull { it.position == destinationNodeLocation } ?: return listOf()

		val closedNodes = engine.trackedSections.filter { !it.navigable }.toMutableList()

		val originNode = allNodes.firstOrNull { it.position == originNodeLocation } ?: return listOf()

		val openNodes = mutableListOf(originNode)
		var currentNode: SectionNode = originNode

		var iterations = 0
		while (currentNode != destinationNode) {
			if (iterations > MAX_A_STAR_ITERATIONS) break
			iterations++

			closedNodes += currentNode

			val nextNode = searchNeighbors(
				currentNode,
				allNodes,
				closedNodes,
				destinationNode,
				previousPositions
			) ?: return openNodes

			currentNode = nextNode
			openNodes += nextNode

			continue
		}

		return openNodes
	}

	@Synchronized
	private fun searchNeighbors(
		previousNode: SectionNode,
		allNodes: Collection<SectionNode>,
		closedNodes: Collection<SectionNode>,
		destinationNode: SectionNode,
		previousPositions: Collection<SectionNode>
	): SectionNode? {
		val neighbors = getNeighborNodes(previousNode, allNodes)

		if (neighbors.isEmpty()) throw PathfindingException("No neighbors for $previousNode!")

		neighbors.forEach { if (it == destinationNode) return it }
		if (!previousNode.navigable) return neighbors.firstOrNull { it.navigable }

		return neighbors
			.filter { it.navigable }
			.filter { !closedNodes.contains(it) }
			.associateWith {
				var distance = getEstimatedCostEuclidean(it, destinationNode)

				// Discourage moving into nodes with neighbors that aren't navigable
				if (getNeighborNodes(it, allNodes).any { neighborNeighbor -> !neighborNeighbor.navigable }) distance += 100

				// Discourage moving into nodes with neighbors that have been moved through already
				if (previousPositions.contains(it)) distance += 100

				distance += it.inhabitedTime

				distance
			}
			.minByOrNull { it.value }?.key
	}

	private fun getNeighborNodes(node: SectionNode, all: Collection<SectionNode>): Set<SectionNode> {
		val nodes = mutableSetOf<SectionNode>()
		val neighbors = getNeighbors(node)

		return all.filterTo(nodes) { neighbors.contains(it.position) }
	}

	private fun getNeighbors(node: SectionNode): Set<Vec3i> {
		val (x, y, z) = node.position
		val sections = mutableSetOf<Vec3i>()

		for (face in Adjacent.values()) {
			val newX = x + face.modX
			val newY = y + face.modY
			val newZ = z + face.modZ

			sections += Vec3i(newX, newY, newZ)
		}

		return sections
	}

	/** Gets a destination node location */
	private fun getDestinationNode(origin: Location, destination: Vec3i, searchDistance: Int): Vec3i {
		val vector = destination.toVector().subtract(origin.toVector()).normalize().multiply((searchDistance - 1).shl(4))

		val (x, y, z) = Vec3i(origin.clone().add(vector))
		val chunkX = x.shr(4)
		val chunkZ = z.shr(4)
		val sectionMinY = (y - origin.world.minHeight).shr(4)

		return Vec3i(chunkX, sectionMinY, chunkZ)
	}

	// Use squares for estimation
	private fun getEstimatedCostEuclidean(node: SectionNode, destinationNode: SectionNode): Int =
		distanceSquared(node.position, destinationNode.position)

	fun getEstimatedCostManhattan(node: SectionNode, destination: Vec3i): Double {
		val origin = node.center.toVector()

		destination.toVector()

		val xDiff = abs(origin.x - destination.x)
		val yDiff = abs(origin.y - destination.y)
		val zDiff = abs(origin.z - destination.z)

		return xDiff + yDiff + zDiff
	}

	class PathfindingException(message: String?) : Exception(message)

	enum class Adjacent(val modX: Int, val modY: Int, val modZ: Int, val cost: Int) {
		NORTH(0, 0, -1, 100),
		UP_NORTH(0, 1, -1, 141),
		DOWN_NORTH(0, -1, -1, 141),
		NORTH_EAST(1, 0, -1, 141),
		NORTH_WEST(-1, 0, -1, 141),
		UP_NORTH_EAST(1, 1, -1, 144),
		UP_NORTH_WEST(-1, 1, -1, 144),
		DOWN_NORTH_EAST(1, -1, -1, 144),
		DOWN_NORTH_WEST(-1, -1, -1, 144),
		EAST(1, 0, 0, 100),
		UP_EAST(1, 1, 0, 141),
		DOWN_EAST(1, -1, 0, 141),
		SOUTH(0, 0, 1, 100),
		UP_SOUTH(0, 1, 1, 141),
		DOWN_SOUTH(0, -1, 1, 141),
		SOUTH_EAST(1, 0, 1, 141),
		SOUTH_WEST(-1, 0, 1, 141),
		UP_SOUTH_EAST(1, 1, 1, 144),
		UP_SOUTH_WEST(-1, 1, 1, 144),
		DOWN_SOUTH_EAST(1, -1, 1, 144),
		DOWN_SOUTH_WEST(-1, -1, 1, 144),
		WEST(-1, 0, 0, 100),
		UP_WEST(-1, 1, 0, 141),
		DOWN_WEST(-1, -1, 0, 141),
		UP(0, 1, 0, 100),
		DOWN(0, -1, 0, 100),
	}
}
