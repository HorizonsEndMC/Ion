package net.horizonsend.ion.server.features.starship.control.movement

import com.google.common.collect.HashMultimap
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.ai.module.combat.AimingModule
import net.horizonsend.ion.server.features.ai.module.debug.AIDebugModule
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.input.AIInput
import net.horizonsend.ion.server.features.starship.control.weaponry.StarshipWeaponry
import net.horizonsend.ion.server.features.starship.subsystem.misc.MiningLaserSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PointDefenseSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.AIHeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.AIPhaserWeaponSystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ArsenalRocketStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.HeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.PhaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.RocketWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TorpedoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TriTurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

object AIControlUtils {
	/** Direct an AI ship to shift fly in a direction. Will stop moving if provided a null vector **/
	fun shiftFlyInDirection(controller: AIController, direction: Vector?) {
		(controller.movementHandler.input as? AIInput)?.updateInput(direction)
	}

	/** Will stop moving if provided a null location **/
	fun shiftFlyToLocation(controller: AIController, starshipLocation: Vec3i, location: Location?) = Tasks.async {
		shiftFlyToLocation(controller, starshipLocation, location?.let { Vec3i(it) })
	}

	fun shiftFlyToLocation(controller: AIController, starshipLocation: Vec3i, location: Vec3i?) = Tasks.async {
		if (location == null) {
			(controller.movementHandler.input as? AIInput)?.updateInput(null)
			return@async
		}

		val direction = location.minus(starshipLocation).toVector()
		shiftFlyInDirection(controller, direction)
	}

	/** Will stop moving if provided a null player **/
	fun shiftFlyTowardsPlayer(controller: AIController, starshipLocation: Vec3i, player: Player?) =
		shiftFlyToLocation(controller, starshipLocation, player?.location)

	/** Will attempt to face in the specified direction **/
	fun faceDirection(controller: AIController, direction: BlockFace) {
		if (controller.starship !is ActiveControlledStarship) return

		if (direction == BlockFace.UP || direction == BlockFace.DOWN) return
		val starship = (controller.starship as? ActiveControlledStarship) ?: return

		val isFacing = controller.starship.forward
		if (starship.pendingRotations.isNotEmpty()) return

		// 1.5 sec turn delay
		if (System.currentTimeMillis() - controller.lastRotation < 1500) return

		when (direction) {
			// Facing the same direction
			isFacing -> return

			// New direction is to the right
			isFacing.rightFace -> starship.tryRotate(true)

			// New direction is to the left
			isFacing.leftFace -> starship.tryRotate(false)

			// New direction is backwards, just rotate either way
			isFacing.oppositeFace -> {
				starship.tryRotate(true)
				starship.tryRotate(true)
			}

			else -> return
		}

		controller.lastRotation = System.currentTimeMillis()
	}

	fun shootAtPlayer(
		controller: AIController,
		player: Player,
		leftClick: Boolean,
		controllerLoc: Vector? = null,
		weaponSet: String? = null,
	) {
		shootAtTarget(
			controller,
			player.location.toVector(),
			leftClick,
			controllerLoc,
			weaponSet
		)
	}

	fun shootAtTarget(
		controller: AIController,
		target: Vector,
		leftClick: Boolean,
		controllerLoc: Vector? = null,
		weaponSet: String? = null,
	) {
		val originLocation = controllerLoc ?: controller.starship.centerOfMass.toVector()

		val direction = target.clone().subtract(originLocation)

		shootInDirection(
			controller,
			direction,
			leftClick,
			true,
			controllerLoc,
			weaponSet
		)
	}

	fun shootInDirection(
		controller: AIController,
		direction: Vector,
		leftClick: Boolean,
		manual: Boolean = true,
		target: Vector? = null,
		weaponSet: String? = null,
		controllerLoc: Location? = null
	) {


		if (AIDebugModule.showAims) {
			if (target != null) {
				AimingModule.showAims(controller.getWorld(),target,leftClick)
			}
		}

		if (!AIDebugModule.fireWeapons) {
			return
		}

		val damager = controller.damager
		val originLocation = controllerLoc ?: controller.starship.centerOfMass.toLocation(controller.starship.world)

		if (!leftClick) {
			val elapsedSinceRightClick = System.nanoTime() - StarshipWeaponry.rightClickTimes.getOrDefault(damager, 0)

			if (elapsedSinceRightClick > TimeUnit.MILLISECONDS.toNanos(250)) {
				StarshipWeaponry.rightClickTimes[damager] = System.nanoTime()
				return
			}

			StarshipWeaponry.rightClickTimes.remove(damager)
		}

		StarshipWeaponry.manualFire(
			controller.damager,
			controller.starship,
			leftClick,
			vectorToBlockFace(direction),
			direction,
			target ?: StarshipWeaponry.getTarget(originLocation, direction, controller.starship),
			weaponSet,
			manual
		)
	}

	fun setAutoWeapons(controller: AIController, node: String, target: AutoTurretTargeting.AutoTurretTarget<*>?) {
		val starship = controller.starship

		if (target != null) starship.autoTurretTargets[node] = target
			else starship.autoTurretTargets.remove(node)
	}

	fun setAutoWeapons(controller: AIController, node: String, target: Player?) {
		setAutoWeapons(controller, node, target?.let { AutoTurretTargeting.target(it) })
	}

	fun setAutoWeapons(controller: AIController, node: String, target: ActiveStarship?) {
		setAutoWeapons(controller, node, target?.let { AutoTurretTargeting.target(it) })
	}

	fun unSetAllWeapons(controller: AIController) {
		controller.starship.autoTurretTargets.clear()
	}

	fun guessWeaponSets(starship: Starship, controller: AIController) {
		debugAudience.information("weaponSets ${starship.weaponSets.keySet()}")

		//easier to manipulate
		val starshipWeaponSets = starship.weaponSets.asMap().map { (key, values) -> key to values.toSet()}

		val accountedFor : MutableSet<String> = mutableSetOf()

		//special cases first
		val predicate = {weapon :WeaponSubsystem ->
			weapon is MiningLaserSubsystem ||
			weapon is ArsenalRocketStarshipWeaponSubsystem ||
			weapon is PointDefenseSubsystem ||
			weapon is RocketWeaponSubsystem
		}
		val specialWeapons = starship.weapons.filter(predicate)
		for (specialWeapon in specialWeapons) {
			val weaponSets = starshipWeaponSets.filter{ it.second.contains(specialWeapon) }
			for (weaponSet in weaponSets) {
				if (accountedFor.contains(weaponSet.first)) continue
				controller.addSpecialSet(weaponSet.first,0.0,weaponSet.second.first().balancing.range)
				accountedFor.add(weaponSet.first)
			}
		}

		//first we have to divy up manual weapons using heavy weapon range as a guide
		//get all unique heavy weapons and sort by priority
		val heavyWeapons = starship.weapons.filter { it is HeavyWeaponSubsystem }.sortedBy {
			weaponSortMap.getOrDefault(it::class,2) }.distinctBy { it::class }.toMutableList()
		var initialHeavyRange = 0.0
		var initialHeavyAutoRange = 0.0
		var heavyWeapon : WeaponSubsystem
		//go through weapons until heaves are exausted, or the light weapons range on shared heavy+light set is less than
		//the next heavy weapon
		heavyWeapons@ while (heavyWeapons.isNotEmpty()) {
			heavyWeapon = heavyWeapons.removeFirst()
			val weaponSets = starshipWeaponSets.filter{ it.second.contains(heavyWeapon) }
				.sortedBy{ it.second.filter { weapon -> weapon !is HeavyWeaponSubsystem}.minOfOrNull { weapon -> weapon.balancing.range } }
			for (weaponSet in weaponSets) {
				if (accountedFor.contains(weaponSet.first)) continue
				if (weaponSet.second.all { it is AutoWeaponSubsystem }) continue // this is an auto set
				val heavyWeaponRange = heavyWeapon.balancing.range
				val lightWeaponRange = weaponSet.second.minOf { weapon -> weapon.balancing.range }
				if (heavyWeaponRange < lightWeaponRange) {
					controller.addManualSet(weaponSet.first,initialHeavyRange,heavyWeaponRange)
					initialHeavyRange = heavyWeaponRange
					if (heavyWeapon !is AutoWeaponSubsystem) {
						initialHeavyAutoRange = heavyWeaponRange} // push the auto heavy weapon out
					accountedFor.add(weaponSet.first)
					continue@heavyWeapons
				} else {
					controller.addManualSet(weaponSet.first,initialHeavyRange,lightWeaponRange)
					initialHeavyRange = lightWeaponRange
					if (heavyWeapon !is AutoWeaponSubsystem) {
						initialHeavyAutoRange = lightWeaponRange} // push the auto heavy weapon out
					accountedFor.add(weaponSet.first)
					continue
				}
			}
		}
		// now that heavies in manual sets are covered, we have to take care of the remaining manual sets and autosets
		var initialLightRange = 0.0
		var initialAutoRange = 0.0
		val weaponSets = starshipWeaponSets.filter{ !accountedFor.contains(it.first) }
			.sortedBy { it.second.minOf{weapon -> weapon.balancing.range} }
		for (weaponSet in weaponSets) {
			if (weaponSet.second.all { it is AutoWeaponSubsystem }) {// this is an auto set
				if (weaponSet.second.any { it is HeavyWeaponSubsystem }) {// this is an auto heavy set
					val heavyWeaponRange = weaponSet.second.filter { it is HeavyWeaponSubsystem }.minOf { weapon -> weapon.balancing.range }
					controller.addAutoSet(weaponSet.first,initialHeavyAutoRange,heavyWeaponRange)
					initialHeavyAutoRange = heavyWeaponRange
					accountedFor.add(weaponSet.first)
					continue
				}
				val lightWeaponRange = weaponSet.second.minOf { weapon -> weapon.balancing.range }
				controller.addAutoSet(weaponSet.first,initialAutoRange,lightWeaponRange)
				accountedFor.add(weaponSet.first)
				continue
			}
			val lightWeaponRange = weaponSet.second.minOf { weapon -> weapon.balancing.range }
			controller.addManualSet(weaponSet.first,initialLightRange,lightWeaponRange)
			initialLightRange = lightWeaponRange
			accountedFor.add(weaponSet.first)
			continue
		}
		debugAudience.information("Accounted for ${accountedFor.size} of ${starship.weaponSets.keySet().size}")
		val notCounted = starship.weaponSets.keySet().subtract(accountedFor)
		if (notCounted.isNotEmpty()) debugAudience.alert("Some sets not accounted for! ${notCounted.joinToString { it }}")
	}

	private val weaponSortMap = mapOf(
		PhaserWeaponSubsystem::class to 0,
		AIPhaserWeaponSystem::class to 0,
		HeavyLaserWeaponSubsystem::class to 1,
		AIHeavyLaserWeaponSubsystem::class to 1,
		TriTurretWeaponSubsystem::class to 3,
		TorpedoWeaponSubsystem::class to 4,
	)

	private fun weaponSetFilter(weaponsets : HashMultimap<String, WeaponSubsystem>, predicate : (Set<WeaponSubsystem> )-> Boolean) {

	}
}
