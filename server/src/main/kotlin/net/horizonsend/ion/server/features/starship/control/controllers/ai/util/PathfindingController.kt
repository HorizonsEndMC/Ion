package net.horizonsend.ion.server.features.starship.control.controllers.ai.util

import net.horizonsend.ion.server.features.starship.control.movement.AIPathfinding
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.World

interface PathfindingController {
	/** Each Vec3i represents a chunk section. They are marked as navigable or not. */
	val trackedSections: MutableSet<AIPathfinding.SectionNode>
	var chunkSearchRadius: Int

	fun getWorld(): World

	fun getSectionPositionOrigin(): Vec3i {
		val center = Vec3i(getCenter())
		val world = getWorld()

		val x = center.x.shr(4)
		val z = center.z.shr(4)
		val y = (center.y - world.minHeight).shr(4)

		return Vec3i(x, y, z)
	}

	/** Returns a location representation of the center of the ship */
	fun getCenter(): Location
	fun getCenterVec3i(): Vec3i

	/** Returns an ordered list of points to navigate towards the provided destination */
	fun getNavigationPoints(
		destination: Vec3i,
		completedObjectives: Collection<AIPathfinding.SectionNode> = listOf()
	): List<AIPathfinding.SectionNode> = AIPathfinding.findNavigationNodes(this, destination, completedObjectives)
}
