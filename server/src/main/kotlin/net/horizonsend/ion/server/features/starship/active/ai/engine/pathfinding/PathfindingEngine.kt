package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.starship.active.ai.AIManager
import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.MovementEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.VariableObjectiveController
import net.horizonsend.ion.server.features.starship.control.movement.AIPathfinding
import net.horizonsend.ion.server.features.starship.movement.RotationMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.highlightBlock
import net.horizonsend.ion.server.miscellaneous.utils.highlightBlocks
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue

open class PathfindingEngine(
	controller: AIController,
	var destination: Vec3i?
) : AIEngine(controller) {
	/** How many ticks between the clearing of the tracked sections, -1 to never clear */
	var clearInterval: Int = 100

	open fun passToMovementEngine(movementEngine: MovementEngine) {
		movementEngine.destination = getFirstNavPoint()
	}

	open val tickInterval: Int = 1

	/** Update the tracked environment around the ship */
	var center: Vec3i? = null

	fun realToSectionPos(realPosition: Vec3i): Vec3i {
		val x = realPosition.x.shr(4)
		val z = realPosition.z.shr(4)
		val y = (realPosition.y - world.minHeight).shr(4)

		return Vec3i(x, y, z)
	}

	fun getSectionPositionOrigin(): Vec3i {
		val center = Vec3i(getCenter())

		return realToSectionPos(center)
	}

	var chunkSearchRadius: Int = IonServer.server.viewDistance

	/** Store the currently tracked section nodes */
	val trackedSections = mutableSetOf<AIPathfinding.SectionNode>()

	fun getOriginNode(): AIPathfinding.SectionNode {
		val centerPos = getSectionPositionOrigin()
		val originCandidate = trackedSections.firstOrNull { it.position == centerPos }

		if (originCandidate != null) return originCandidate

		AIPathfinding.adjustTrackedSections(this, true)
		// The origin node must be present after tracking sections
		return trackedSections.first { it.position == centerPos }
	}

	/**
	 * Gets a destination node location
	 *
	 * @param destination, the destination, in real coordinates
	 * */
	fun getDestinationNode(): AIPathfinding.SectionNode {
		val origin = getOriginNode()

		val destination = destination ?: return origin
		val destinationNodePosition = realToSectionPos(destination)

		// If the destination node is already tracked, return it
		AIPathfinding.SectionNode.get(trackedSections, destinationNodePosition)?.let { return it }

		// Get a destination within the tracked nodes in the direction of the destination
		val vectorToDestination = destinationNodePosition.toVector().subtract(origin.position.toVector()).normalize().multiply(chunkSearchRadius)
		val trackedDestinationNode = origin.position + Vec3i(vectorToDestination)

		// Get it from the tracked sections
		val newDestination = AIPathfinding.SectionNode.get(trackedSections, trackedDestinationNode)

		// If its present and navigable, return it
		if (newDestination?.navigable == true) return newDestination

		// Search closer if its null
		if (newDestination == null) {
			var newDistance = 0.8

			while (newDistance > 0) {
				val multiply = chunkSearchRadius * newDistance
				val newVector = destinationNodePosition.toVector().subtract(origin.position.toVector()).normalize().multiply(multiply)

				val closerDestination = AIPathfinding.SectionNode.get(trackedSections, origin.position + Vec3i(newVector))

				if (closerDestination?.navigable == true) return closerDestination

				newDistance -= 0.2
			}
		}

		// If it's still null, something's wrong.
		if (newDestination?.navigable == null) return origin

		// At this point the destination is non-null, but unnavigable. Just find the nearest navigable node
		newDestination.getNeighbors(trackedSections).values.firstOrNull { it.navigable }?.let { return it }

		// None of the neighbors are navigable either
		newDestination.getNeighbors(trackedSections).values.firstNotNullOfOrNull { immediateNeighbors ->
			immediateNeighbors.getNeighbors(trackedSections).values.firstOrNull { neighborNeighbors -> neighborNeighbors.navigable }
		}?.let { return it }

		// Give up
		return origin
	}

	/** General update task for pathfinding */
	fun updatePathfinding() {
		val newCenter = getSectionPositionOrigin()
		debugAudience.debug("Updating pathfinding")

		// It has not left the old section center, return
		if (center == newCenter && chartedPath.isNotEmpty() && trackedSections.isNotEmpty()) {
			debugAudience.debug("Returning because ship has not moved")
			return
		}

		// Mark the center as the new center
		center = newCenter

		debugAudience.audiences().filterIsInstance<Player>().forEach {
			player -> trackedSections.filter { !it.navigable }.forEach { it.highlight(player) }
		}

		// Store the previous center
		val loadChunks = center	== null
		AIPathfinding.adjustTrackedSections(this, loadChunks)

		// Set the destination if it has changed
		if (controller is VariableObjectiveController) {
			destination = controller.getObjective()
		}

		// Update the path
		debugAudience.debug("Updating Charted path")
		updateChartedPath()
		debugAudience.debug("Updated Charted path")
	}

	/** A queue containing the section nodes along the charted path */
	private val chartedPath = LinkedBlockingQueue<AIPathfinding.SectionNode>(10)

	/** When it has reached an objective, the previous ones will be stored here. */
	private val previousSections = LinkedBlockingQueue<AIPathfinding.SectionNode>(6)

	/** Updates the stored path with  */
	private fun updateChartedPath() {
		debugAudience.debug("Updating Charted Path")

		// This list is created with the closest node at the first index, and the destination as its final.
		// When put into the list queue, the closest will be first.
		val points = AIPathfinding.pathfind(this)
		debugAudience.audiences().filterIsInstance<Player>().forEach { player -> points.forEach { point -> point.node.highlight(player, 200L) } }
		debugAudience.debug("Found points: $points")

		chartedPath.clear()

		for (point in points) {
			debugAudience.highlightBlock(point.node.center, 5L)
			chartedPath.offer(point.node)
		}
	}

	/** Once the first objective in pathfinding has been reached, remove it from the queue. */
	private fun objectiveCompleted() {
		if (chartedPath.isEmpty()) return

		val objective = chartedPath.remove()
		previousSections.offer(objective)
	}

	private var previousTask: CompletableFuture<Any> = CompletableFuture.completedFuture(Any())
	var ticks = 0

	/** Called when the ship moves. */
	override fun onMove(movement: StarshipMovement) {
		when (movement) {
			is TranslateMovement -> updatePathfinding()
			is RotationMovement -> return
		}
	}

	/** On the ticking of the controller */
	override fun tick() {
		if (!previousTask.isDone) {
			debugAudience.debug("Previous task is not completed")
			return
		}
		if (ticks % tickInterval != 0) return

		ticks++
		navigate()
	}

	/** Main navigation loop */
	open fun navigate(): Future<*> = submitTask {
		debugAudience.debug("Navigating")
		previousTask = CompletableFuture<Any>()

		val run = runCatching {
			// See if the objective has changed
			if (ticks != -1 && ticks % clearInterval == 0) {
				trackedSections.clear()
			}

			debugAudience.debug("Updating pathfinding")
			updatePathfinding()
			debugAudience.debug("Updating pathfinding completed")

			debugAudience.highlightBlocks(chartedPath.map { it.center }, 5L)
		}

		previousTask.complete(Any())

		val exception = run.exceptionOrNull() ?: return@submitTask

		log.error("Pathfinding generated an exception! $exception")
		exception.printStackTrace()
	}

	/** Poll at the charted path to get the flight direction to the first objective */
	open fun getFirstNavPoint(): Vec3i? {
		val destination = this.destination

		var objective: Vec3i = getImmediateNavigationObjective()?.center ?: destination ?: return null

		val distance = getDistanceSquaredToDestination() ?: Int.MAX_VALUE
		if (distance < 512) objective = destination ?: return null

		return objective
	}

	private var lastCompletedSection = AIPathfinding.SectionNode(world, getSectionPositionOrigin(), true)
	/** Polls the charted path for the first position in the path */
	private fun getImmediateNavigationObjective(): AIPathfinding.SectionNode? = chartedPath.minByOrNull {
		val origin = getSectionPositionOrigin()
		val (x, y, z) = origin

		// Don't navigate to a completed objective
		if (it.position == origin || it.position == lastCompletedSection.position) {
			lastCompletedSection = it
			return@minByOrNull Double.MAX_VALUE
		}

		it.position.distance(x, y, z)
	}

	private fun getDistanceSquaredToDestination(): Int? = destination?.let { distanceSquared(getCenterVec3i(), Vec3i(it)) }
	private fun submitTask(task: () -> Unit) = AIManager.navigationThread.submit(task)
}
