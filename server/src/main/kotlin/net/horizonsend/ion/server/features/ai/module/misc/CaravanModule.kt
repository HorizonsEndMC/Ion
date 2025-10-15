package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.ai.convoys.AIConvoyTemplate
import net.horizonsend.ion.server.features.ai.convoys.ConvoyContext
import net.horizonsend.ion.server.features.ai.convoys.ConvoyRoute
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.GoalTarget
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.features.starship.fleet.Fleet
import net.horizonsend.ion.server.features.starship.fleet.FleetLogic
import net.horizonsend.ion.server.features.starship.fleet.FleetMember
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.ref.WeakReference

class CaravanModule(
	controller: AIController,
	val fleet: Fleet,
	val template: AIConvoyTemplate<out ConvoyContext>,
	val source: Location,
	val route: ConvoyRoute
) : AIModule(controller) {

	var target: AITarget = GoalTarget(Vec3i(source), source.world, false)
	var isTraveling = true

	val tickRate = 20 * 2
	var ticks = 0 + randomInt(0, tickRate) //randomly offset targeting updates

	init {
		initialize()
	}

	fun initialize() {
		if (fleet.logic != null) return
		val logic = CaravanFleetLogic(template, source, route, fleet)
		fleet.logic = logic

		debugAudience.debug("Added a caravan fleet logic to this ships fleet: ${this.starship.getDisplayNamePlain()}")
	}

	override fun tick() {

		ticks++
		if (ticks % tickRate != 0) return
		ticks = 0

		val enmity = controller.getCoreModuleByType<EnmityModule>() ?: return
		val leader = fleet.leader ?: return

		val isLeader = (leader as? FleetMember.AIShipMember)?.shipRef?.get() == starship
		val newTarget = if (isLeader) {
			getLeaderTarget()
		} else {
			getFollowingTarget() ?: return
		}
		if (target != newTarget) {
			enmity.removeTarget(target)
			target = newTarget
		}

		val tolerance = if (target !is GoalTarget) 800.0 else 100.0

		if (starship.world == target.getWorld() &&
			starship.centerOfMass.toVector().distance(target.getVec3i().toVector()) < tolerance
		) {
			enmity.removeTarget(target) //prevent crowding near the destination
			isTraveling = false
		} else {
			if ((fleet.logic as? CaravanFleetLogic)?.isTraveling == true)
				enmity.addTarget(target, decay = false, aggroed = true)
			isTraveling = true
		}
	}


	fun getLeaderTarget(): AITarget {
		val logic = fleet.logic as CaravanFleetLogic
		val goalTarget = GoalTarget(Vec3i(logic.currentDestination), logic.currentDestination.world, hyperspace = false)
		return goalTarget
	}

	fun getFollowingTarget(): AITarget? {
		val leader = fleet.leader ?: return null

		if (!fleet.hasValidLeader()) return null

		val leaderLoc = fleet.getLeaderLocation() ?: return null

		val closeToLeader = starship.world == leaderLoc.world &&
			starship.centerOfMass.toVector().distance(leaderLoc.toVector()) < 800.0

		if (closeToLeader) return getLeaderTarget()

		// We're far from the leader, so follow the leader directly
		return when (leader) {
			is FleetMember.PlayerMember -> {
				val player = Bukkit.getPlayer(leader.uuid) ?: return null
				PlayerTarget(player, attack = false) //TODO cast to piloted starship
			}

			is FleetMember.AIShipMember -> {
				val ship = leader.shipRef.get() ?: return null
				StarshipTarget(ship, attack = false)
			}
		}
	}
}

class CaravanFleetLogic(
	val template: AIConvoyTemplate<out ConvoyContext>,
	val source: Location,
	val route: ConvoyRoute,
	fleet: Fleet
) : FleetLogic(fleet) {
	var isTraveling = true
	var currentDestination = advanceDestination() ?: source
	val waitTime = 60 * 3 // number of seconds to wait at a location

	fun advanceDestination(): Location? {
		val next = route.advanceDestination()
		return next
	}

	fun assignLeader() {
		val largest = fleet.members
			.filterIsInstance<FleetMember.AIShipMember>()
			.mapNotNull { it.shipRef.get() }
			.maxByOrNull { it.initialBlockCount }

		if (largest != null) {
			fleet.leader = FleetMember.AIShipMember(WeakReference(largest))
			debugAudience.debug("Assigned fleet leader to ${largest.getDisplayNamePlain()}")
		}
	}

	fun disband() {
		fleet.members.forEach {
			if (it !is FleetMember.AIShipMember) return@forEach
			val ship = it.shipRef.get() ?: return@forEach
			StarshipDestruction.vanish(ship)
		}
		fleet.members.forEach {
			if (it !is FleetMember.PlayerMember) return@forEach
			val player = Bukkit.getPlayer(it.uuid) ?: return@forEach
			player.information("Convoy has finished its loop and is now disbanding")
		}
	}

	override fun tick() {
		if (!isTraveling) return
		// Use shared state and tick all members
		if (fleet.leader == null ||
			fleet.leader is FleetMember.AIShipMember && (fleet.leader as FleetMember.AIShipMember).shipRef.get() == null
		) {
			assignLeader()
		}
		val currentLoc = fleet.getLeaderLocation() ?: return

		// Check world and distance
		if (currentLoc.world == currentDestination.world &&
			currentLoc.distance(currentDestination) < 100.0
		) {

			val nextDestination = advanceDestination()

			if (nextDestination == null) {
				disband()
				return
			}
			isTraveling = false

			// Schedule resumption after waitTime seconds
			Tasks.syncDelay(waitTime * 20L) { // 20 ticks per second
				currentDestination = nextDestination
				debugAudience.debug("New convoy destination: $nextDestination")
				isTraveling = true
			}
		}
	}
}
