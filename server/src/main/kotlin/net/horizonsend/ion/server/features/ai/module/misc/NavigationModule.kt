package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.targeting.EmityModule
import net.horizonsend.ion.server.features.ai.util.GoalTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.multiblock.type.navigationcomputer.VerticalNavigationComputerMultiblockAdvanced
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace.getHyperspaceWorld
import net.horizonsend.ion.server.features.starship.hyperspace.MassShadows
import net.horizonsend.ion.server.features.starship.subsystem.misc.HyperdriveSubsystem
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.horizonsend.ion.server.miscellaneous.utils.normalize
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.ln
import kotlin.math.roundToInt

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

	private fun attemptHyperdrive() {
		if (Hyperspace.isWarmingUp(starship) || Hyperspace.isMoving(starship)) return
		if (MassShadows.find(world, location.x.toDouble(), location.z.toDouble()) != null) {
			starship.debug("In Mass shadow, doing non hyperspace navigation")
			navigationTarget.hyperspace = false
			return
		}
		if (!world.hasFlag(WorldFlag.SPACE_WORLD)) {
			starship.debug("Not in a space world, cant use hyperdrive")
			navigationTarget.hyperspace = false
			return
		}

		val dist = navigationTarget.position.toVector().distance(location.toVector())
		if (starship.beacon != null && starship.beacon!!.radius > dist) { // if nearby beacon use beacon
			val hyperdrive = starship.hyperdrives.firstOrNull{it.isIntact()}
			if (hyperdrive == null) {
				starship.debug("Need hyperdrive to use beacon, giving up")
				navigationTarget.hyperspace = false
				setOverride(null) // give up navigation
				return
			}
			navigationTarget.hyperspace = true
			starship.debug("Using beacon")
			useBeacon(hyperdrive)
			return
		}

		//navigating intrasystem
		val hyperdrive = starship.hyperdrives.firstOrNull{it.isIntact()}
		if (hyperdrive == null) {
			starship.debug("Missing hyperdrive, cruising instead")
			navigationTarget.hyperspace = false
			return
		}
		navigationTarget.hyperspace = true
		val range = (starship.balancing.hyperspaceRangeMultiplier
			* VerticalNavigationComputerMultiblockAdvanced.baseRange).toInt()
		tryJump(hyperdrive,navigationTarget.getLocation(),range)

	}

	private fun useBeacon(hyperdrive : HyperdriveSubsystem) {
		val beacon = starship.beacon!!
		val other = beacon.exits?.randomOrNull() ?: beacon.destination
		tryJump(hyperdrive, other.toLocation(), Int.MAX_VALUE)
	}

	private fun tryJump(hyperdrive : HyperdriveSubsystem, jumpLocation: Location, maxRange : Int) {
		if (getHyperspaceWorld(world) == null) {
			starship.debug("no hyperspace world attached to space world")
			return
		}
		if (!jumpLocation.world.worldBorder.isInside(jumpLocation)) {
			starship.debug("Target is outside the world border!")
		}

		if (starship.cruiseData.velocity.lengthSquared() != 0.0) {
			starship.debug("Starship is cruising; jump aborted.")
			StarshipCruising.stopCruising(starship.controller, starship)
			return
		}

		var x1: Int = jumpLocation.x.toInt()
		var z1: Int = jumpLocation.z.toInt()

		val origin: Vector = starship.centerOfMass.toVector()
		val distance: Double = distance(origin.x, 0.0, origin.z, x1.toDouble(), 0.0, z1.toDouble())

		if (distance > maxRange) {
			val (normalizedX, _, normalizedZ) = normalize(x1 - origin.x, 0.0, z1 - origin.z)
			x1 = (normalizedX * maxRange + origin.x).roundToInt()
			z1 = (normalizedZ * maxRange + origin.z).roundToInt()

			starship.debug(
				"attempted to jump ${distance.toInt()} blocks, " +
					"but navigation computer only supports jumping up to $maxRange blocks! " +
					"Automatically shortening jump. New Coordinates: $x1, $z1"
			)
		}

		starship.debug("Initiating hyperspace jump to ${jumpLocation.world.name} ($x1, $z1)")

		val offset = ln(distance).toInt()

		// don't let it be perfectly accurate
		x1 += randomInt(-offset, offset)
		z1 += randomInt(-offset, offset)

		Hyperspace.beginJumpWarmup(starship, hyperdrive, x1, z1, jumpLocation.world, false)

	}

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

	private fun setOverride(target : GoalTarget?) {
		if (target == null) {
			targetModule.findTargetOverride = null
			return
		}
		targetModule.findTargetOverride = { target}
	}


}
