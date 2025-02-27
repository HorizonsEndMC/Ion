package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.targeting.EmityModule
import net.horizonsend.ion.server.features.ai.util.GoalTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location

class NavigationModule(
	controller : AIController,
	val targetModule : EmityModule,
	val difficulty : DifficultyModule,
	val engageHyperdiveRange : Double = 1000.0,
) : AIModule(controller){
	private val tickRate = 20 * 10
	private var ticks = 0
	private val targetLocation : Location? get() {
		val target = targetModule.findTargetAnywhere()
		val location : Location?
		if (target is StarshipTarget && Hyperspace.isMoving(target.ship)) {
			location = Hyperspace.getJumpDestination(target.ship)!!
		} else {
			location = target?.getLocation()
		}
		return location
	}
	private var hyperdriveNavigate = false
	private var navigate = false
	private var navigationTarget = GoalTarget(starship.centerOfMass,world, false)
	private var lastWorld = world
	private var triggerUpdate = false




	override fun tick() {
		validateNavigation()
		//only try to use hyperdrive when navigation calls for it and after navigation has been evaluated
		if (navigate && hyperdriveNavigate && !triggerUpdate) attemptHyperdrive()

		ticks++
		if (ticks % tickRate != 0 && !triggerUpdate) return
		if (navigate) evaluateNavigation()

	}

	private fun attemptHyperdrive() {}

	/**
	 * Checks if navigation is necessary due to distance and world
	 * Allows for quickly switching to combat mode after jump exits
	 */
	private fun validateNavigation() {
		if (lastWorld != world) {
			lastWorld = world
			triggerUpdate = true
		}

		if (targetLocation == null) { // no target, nothing to navigate to
			navigate = false
			setOverride(null)
			return
		}
		if (targetLocation!!.getWorld() != world) { //need to navigate to a different world
			navigate = true
			return
		}
		val dist = targetLocation!!.toVector().distance(location.toVector())
		if (dist >= engageHyperdiveRange && world.hasFlag(WorldFlag.SPACE_WORLD)) {
			navigate = true
			return
		}
		if (!world.hasFlag(WorldFlag.HYPERSPACE_WORLD)) {
			navigate = false //target is close enough to not require navigation
			setOverride(null)
			return
		} else {} // not sure what the other condition should be

	}

	private fun evaluateNavigation() {
		triggerUpdate = false
		// Current possible scenarios:
		// AI ship is in a planet, target is in space
		// AI ship is in a planet, target is in a different planet
		// AI ship is in space, target is in space
		// AI ship is in space, target is in a planet
		// AI ship is in hyperspace

		//AI ship in hyperspace
		if (world.hasFlag(WorldFlag.HYPERSPACE_WORLD)) return //cant make decisions in hyperspace

		//AI ship in planet
		if (world.hasFlag(WorldFlag.PlANET_WORLD)) { //ship needs to leave the planet
			fromPlanet()
			return
		}
		//AI ship in Space
		if (world.hasFlag(WorldFlag.SPACE_WORLD)) {
			fromSpace()
			return
		}
	}

	private fun fromPlanet() {
		var goalPoint = Vec3i(starship.centerOfMass.x, 400, starship.centerOfMass.z)
		goalPoint += Vec3i(starship.forward.direction.multiply(100.0))
		hyperdriveNavigate = false
		navigationTarget = GoalTarget(goalPoint, world, false)
		setOverride(navigationTarget)
	}

	private fun fromSpace() {
		val dest = if (targetLocation!!.world.hasFlag(WorldFlag.PlANET_WORLD)) { //need to cast to space coordinate
			val spaceworld = Space.getPlanet(targetLocation!!.world)!!.spaceWorld!!
			Space.getPlanet(targetLocation!!.world)!!.location.toLocation(spaceworld)
		} else {
			targetLocation!!
		}

		//Player is in a nearby planet
		//This condition works because we already checked if the ai and target words are different
		//The only way this holds is if there is a planet nearby.
		if (dest.world == world && dest.toVector().distance(location.toVector()) < engageHyperdiveRange) {
			hyperdriveNavigate = false
			navigationTarget = GoalTarget(Vec3i(dest), dest.world, false)
			setOverride(navigationTarget)
			return
		}

		val path = if (targetLocation!!.world.hasFlag(WorldFlag.PlANET_WORLD)) {
			WaypointManager.findShortestPathToPlanet(location.toLocation(world), targetLocation!!.world)
		} else {
			WaypointManager.findShortestPathBetweenLocations(location.toLocation(world), dest)
		}

		if (path?.edgeList.isNullOrEmpty()) { //no path
			hyperdriveNavigate = false
			setOverride(null)
			return
		}
		val jump = path!!.edgeList.first().target.loc
		navigationTarget = GoalTarget(Vec3i(jump), jump.world, true)
		setOverride(navigationTarget)
		hyperdriveNavigate = true
		return
	}

	private fun setOverride(target : GoalTarget?) {}


}
