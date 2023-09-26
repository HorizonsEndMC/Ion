package net.horizonsend.ion.server.features.starship.control.controllers.ai.util

import net.horizonsend.ion.server.features.starship.control.movement.AIPathfinding
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.World

interface PathfindingController {
	/** Each Vec3i represents a chunk section. They are marked as navigable or not. */
	val trackedSections: MutableSet<AIPathfinding.SectionNode>
	var searchDestance: Int

	fun getWorld(): World

	/** Returns a Vec3i representing a chunk position, and level chunk section representing the center of the ship */
	fun getSectionOrigin(): Vec3i {
		val center = Vec3i(getCenter())
		val world = getWorld()

		val x = center.x.shr(4)
		val z = center.z.shr(4)
		val y = (center.y - world.minHeight).shr(4)

		return Vec3i(x, y, z)
	}

	/** Returns a location representation of the center of the ship */
	fun getCenter(): Location

	/** Adjusts the tracked sections used for pathfinding */
	fun adjustPosition(loadChunks: Boolean = false) = AIPathfinding.adjustTrackedSections(this, searchDestance, loadChunks)

	/** Returns an ordered list of points to navigate towards the provided destination */
	fun getNavigationPoints(destination: Vec3i): List<AIPathfinding.SectionNode> = AIPathfinding.findNavigationNodes(this, destination, searchDestance)
}
