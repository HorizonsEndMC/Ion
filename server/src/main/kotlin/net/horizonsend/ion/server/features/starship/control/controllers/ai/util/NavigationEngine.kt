package net.horizonsend.ion.server.features.starship.control.controllers.ai.util

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIManager
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.control.movement.AIPathfinding
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.movement.RotationMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.highlightBlocks
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue

open class NavigationEngine(
	val controller: AIController,
	var destination: Vec3i?
) : PathfindingController {
	protected val log: Logger = LoggerFactory.getLogger(javaClass)

	override fun getCenter(): Location = controller.getCenter()
	override fun getCenterVec3i(): Vec3i = controller.starship.centerOfMass
	override fun getWorld(): World = controller.starship.world

	/** Polls the charted path for the first position in the path */
	private fun getImmediateNavigationObjective(): AIPathfinding.SectionNode? = chartedPath.minBy {
		val (x, y, z) = getSectionPositionOrigin()

		it.location.distance(x, y, z)
	}

	/** Update the tracked environment around the ship */
	var center: Vec3i? = null

	override var chunkSearchRadius: Int = IonServer.server.viewDistance

	/** Store the currently tracked section nodes */
	override val trackedSections = mutableSetOf<AIPathfinding.SectionNode>()

	/** General update task for pathfinding */
	private fun updatePathfinding() {
		val newCenter = getSectionPositionOrigin()

		// It has not left the old section center, return
		if (center == newCenter && chartedPath.isNotEmpty() && trackedSections.isNotEmpty()) {
			log.info("Returning because new center is old center new $newCenter old $center")
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
		if (controller is LocationObjectiveAI) {
			destination = controller.getObjective()
		}

		// Update the path
		updateChartedPath()
	}

	/** A queue containing the section nodes along the charted path */
	private val chartedPath = LinkedBlockingQueue<AIPathfinding.SectionNode>(10)

	/** When it has reached an objective, the previous ones will be stored here. */
	private val previousSections = LinkedBlockingQueue<AIPathfinding.SectionNode>(6)

	/** Updates the stored path with  */
	private fun updateChartedPath() {
		val destination = this.destination ?: return
		debugAudience.debug("Updating Charted Path")

		// This list is created with the closest node at the first index, and the destination as its final.
		// When put into the list queue, the closest will be first.
		val points = AIPathfinding.findNavigationNodes(this, destination, previousSections)

		chartedPath.clear()

		for (point in points) {
			chartedPath.offer(point)
		}
	}

	/**
	 * Checks to see if it has reached the first objective
	 *
	 * @return true if it has, false otherwise
	 **/
	private fun checkObjective(): Boolean {
		val currentSection = getSectionPositionOrigin()
		val firstObjective = getImmediateNavigationObjective()

		if (currentSection == firstObjective?.location) {
			objectiveCompleted()
			return true
		}

		return false
	}

	/** Once the first objective in pathfinding has been reached, remove it from the queue. */
	private fun objectiveCompleted() {
		if (chartedPath.isEmpty()) return

		val objective = chartedPath.remove()
		previousSections.offer(objective)
	}

	private var previousTask: CompletableFuture<Any> = CompletableFuture.completedFuture(Any())
	var ticks = 0

	/** On the removal of the controller */
	open fun shutDown() {}

	/** Called when the ship moves. */
	fun onMove(movement: StarshipMovement) {
		when (movement) {
			is TranslateMovement -> updatePathfinding()
			is RotationMovement -> return
		}
	}

	/** On the ticking of the controller */
	fun tick() {
		if (!previousTask.isDone) return

		ticks++
		navigate()
	}

	/** Main navigation loop */
	private fun navigate(): Future<*> = submitTask {
		previousTask = CompletableFuture<Any>()

		val run = runCatching {

			// See if the objective has changed
			updatePathfinding()

			debugAudience.information("Charted path size: ${chartedPath.size}")
			debugAudience.highlightBlocks(chartedPath.map { it.center }, 5L)

			// Move along the path
			navigationLoop()

		}

		previousTask.complete(Any())

		val exception = run.exceptionOrNull() ?: return@submitTask

		log.error("Pathfinding generated an exception! $exception")
		exception.printStackTrace()
	}

	/** Handle the movement */
	open fun navigationLoop() {
		val distance = getDistanceSquaredToDestination() ?: return

		when {
			distance >= 250000 -> cruiseLoop()
			distance < 250000 -> shiftFlightLoop()
		}
	}

	open fun cruiseLoop() {
		println("Cruise loop")
		val starship = controller.starship as ActiveControlledStarship
		val isCruising = StarshipCruising.isCruising(starship)

		val direction = getNavDirection()
		val facing = starship.forward

		val blockFace = vectorToBlockFace(direction)

		if (facing != blockFace) {
			Tasks.sync { AIControlUtils.faceDirection(controller, blockFace) }

			// Can't cruise if not facing the right direction
			return
		}

		if (!isCruising) {
			debugAudience.information("Cruise loop: Started Cruising")
			Tasks.sync { StarshipCruising.startCruising(controller, starship, direction) }

			return
		}

		if (starship.cruiseData.targetDir == direction.normalize()) return

		debugAudience.information("Cruise loop: Adjusted Direction")
		Tasks.sync { StarshipCruising.startCruising(controller, starship, direction) }
	}

	var shouldRotateDuringShiftFlight = true

	open fun shiftFlightLoop() = Tasks.sync {
		val destination = this.destination ?: return@sync
		val starship = controller.starship as ActiveControlledStarship
		val isCruising = StarshipCruising.isCruising(starship)

		if (isCruising) {
			StarshipCruising.stopCruising(controller, starship)
		}

		checkObjective()
		val direction = getNavDirection()
		val directionToTarget = destination.minus(getCenterVec3i()).toVector()

		val blockFace = vectorToBlockFace(directionToTarget)

		if (shouldRotateDuringShiftFlight) { AIControlUtils.faceDirection(controller, blockFace) }
		AIControlUtils.shiftFlyInDirection(controller, direction)
	}

	/** Poll at the charted path to get the flight direction to the first objective */
	private fun getNavDirection(): Vector {
		val destination = this.destination ?: return Vector(0.0, 0.0, 0.0)

		var objective: Vec3i = getImmediateNavigationObjective()?.center ?: destination

		val distance = getDistanceSquaredToDestination() ?: return Vector(0.0, 0.0, 0.0)
		if (distance < 512) objective = destination

		val origin = getCenterVec3i()

		return objective.minus(origin).toVector()
	}

	private fun getDistanceSquaredToDestination(): Int? = destination?.let { distanceSquared(getCenterVec3i(), Vec3i(it)) }

	private fun submitTask(task: () -> Unit) = AIManager.navigationThread.submit(task)
}
