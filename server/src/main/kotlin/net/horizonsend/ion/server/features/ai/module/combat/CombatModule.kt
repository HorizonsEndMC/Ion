package net.horizonsend.ion.server.features.ai.module.combat

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.module.debug.AIDebugModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

abstract class CombatModule<T>(
	controller: AIController,
	val difficulty : DifficultyModule,
	val aiming : AimingModule,
	val targetingSupplier: Supplier<T>
) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {

	open var shouldFaceTarget: Boolean = false

	// Turn cooldown adjustment
	private var turnTicks: Int = 0
	var turnCooldown: Int = 20 * 3

	/** Rotate to face a specified blockface */
	protected fun rotateToFace(faceDirection: BlockFace) {
		if (!shouldFaceTarget) return
		if (!CARDINAL_BLOCK_FACES.contains(faceDirection)) throw IllegalArgumentException("Ships can only face cardinal directions!")

		if (turnTicks >= turnCooldown) {
			turnTicks = 0
			return
		}

		turnTicks++

		AIControlUtils.faceDirection(controller, faceDirection)
	}

	/**
	 * Use Vec3i as a target to allow block targeting
	 *
	 * Lambda allows modification of the aiming direction
	 **/
	protected fun fireAllWeapons(origin: Vec3i, target: AITarget, aimAtRandom : Boolean	) {

		val targetPos = target.getVec3i()
		//println("targetPos : $targetPos")
		val targetOffset = target.getVec3i(aimAtRandom).minus(targetPos)

		val distance = target.getLocation().toVector().distance(origin.toVector())

		var weaponSets : List<AIStarshipTemplate.WeaponSet?> = controller.getManualSetsInRange(distance)//?.name?.lowercase()
		if (weaponSets.isEmpty()) weaponSets = listOf(null)

		for (weaponSet in weaponSets){
			val correctedHeavyTarget = if (target is StarshipTarget) {
				aiming.adjustAim(target.ship,origin,weaponSet, false, true)
			} else {targetPos.toVector()}
			//println("weaponset: ${weaponSet?.name?.lowercase()}")
			correctedHeavyTarget.add(targetOffset.toVector())
			//println("correctedHeavyTarget : $correctedHeavyTarget")
			val heavyDirection = aiming.sampleDirection(correctedHeavyTarget.clone().subtract(origin.toVector()).normalize())

			val correctedLightTarget = if (target is StarshipTarget) {
				aiming.adjustAim(target.ship,origin,weaponSet, true, true)
			} else {targetPos.toVector()}
			//println("correctedLightTarget : $correctedLightTarget")
			correctedLightTarget.add(targetOffset.toVector())
			val lightDirection = aiming.sampleDirection(correctedLightTarget.clone().subtract(origin.toVector()).normalize())
			//println("lightDirection : $lightDirection")
			Tasks.sync {
				fireHeavyWeapons(heavyDirection, correctedHeavyTarget, weaponSet = weaponSet?.name?.lowercase(), true)
				fireLightWeapons(lightDirection, correctedLightTarget, weaponSet = weaponSet?.name?.lowercase(), true)
			}
		}
	}

	/** Fires light weapons (left click) in a direction */
	protected fun fireLightWeapons(direction: Vector, target: Vector? = null, weaponSet: String? = null, manual : Boolean) {
		starship.debug("Firing light weapons: Set: $weaponSet")
		AIControlUtils.shootInDirection(controller, direction, leftClick = true, manual = manual, target = target, weaponSet = weaponSet)
	}

	/** Fires heavy weapons (right click) in a direction */
	protected fun fireHeavyWeapons(direction: Vector, target: Vector? = null, weaponSet: String? = null, manual: Boolean) {
		starship.debug("Firing heavy weapons: Set: $weaponSet")
		AIControlUtils.shootInDirection(controller, direction, leftClick = false, manual = manual, target = target, weaponSet = weaponSet)
	}

	/** Updates all auto weapons,that are in range, to fire on the target */
	protected fun handleAutoWeapons(origin: Vec3i, target: AITarget) {

		if (!AIDebugModule.fireWeapons) {
			AIControlUtils.unSetAllWeapons(controller)
			return
		}

		val (x, y, z) = origin
		val distance = target.getVec3i(false).distance(x, y, z)
		val weaponSet = controller.getAutoSetInRange(distance)

		if (weaponSet == null) {
			AIControlUtils.unSetAllWeapons(controller)
			return
		}
		if (!difficulty.aimEverything or (target is PlayerTarget)) {
			AIControlUtils.setAutoWeapons(controller, weaponSet.name.lowercase(), target.getAutoTurretTarget())
			return
		}
		//since we are aiming everything we treat auto weapons as manual weapons
		val dummy : AutoTurretTargeting.AutoTurretTarget<*>? = null
		AIControlUtils.setAutoWeapons(controller, weaponSet.name.lowercase(), dummy)

		val targetPos = target.getVec3i()
		val targetOffset = target.getVec3i(true).minus(targetPos)

		val correctedHeavyTarget = if (target is StarshipTarget) {
			aiming.adjustAim(target.ship,origin,weaponSet, false, false)
		} else {targetPos.toVector()}
		correctedHeavyTarget.add(targetOffset.toVector())
		val heavyDirection = aiming.sampleDirection(correctedHeavyTarget.clone().subtract(origin.toVector()).normalize())

		val correctedLightTarget = if (target is StarshipTarget) {
			aiming.adjustAim(target.ship,origin,weaponSet, true, false)
		} else {targetPos.toVector()}
		correctedLightTarget.add(targetOffset.toVector())
		val lightDirection = aiming.sampleDirection(correctedLightTarget.clone().subtract(origin.toVector()).normalize())

		Tasks.sync {
			fireHeavyWeapons(heavyDirection, correctedHeavyTarget, weaponSet = weaponSet.name.lowercase(), false)
			fireLightWeapons(lightDirection, correctedLightTarget, weaponSet = weaponSet.name.lowercase(), false)
		}
	}
}

abstract class MultiTargetCombatModule(
	controller: AIController,
	difficulty: DifficultyModule,
	aiming : AimingModule,
	targetingSupplier: Supplier<List<AITarget>>
) : CombatModule<List<AITarget>>(controller, difficulty,aiming, targetingSupplier) {
}

abstract class SingleTargetCombatModule(
	controller: AIController,
	difficulty: DifficultyModule,
	aiming : AimingModule,
	targetingSupplier: Supplier<AITarget?>
) : CombatModule<AITarget?>(controller, difficulty,aiming,targetingSupplier) {

}


