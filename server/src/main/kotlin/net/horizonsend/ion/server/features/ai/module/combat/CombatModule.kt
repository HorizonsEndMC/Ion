package net.horizonsend.ion.server.features.ai.module.combat

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
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

abstract class CombatModule(
	controller: AIController,
	val difficulty : DifficultyModule,
	val targetingSupplier: Supplier<AITarget?>
) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
	val shotDeviation: Double get () {return difficulty.shotVariation + 0.025}

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
	protected fun fireAllWeapons(origin: Vec3i, target: Vector, direction: Vector) {

		val shootDirection = sampleDirection(direction)

		val distance = target.distance(origin.toVector())

		var weaponSets : List<AIStarshipTemplate.WeaponSet?> = controller.getManualSetsInRange(distance)//?.name?.lowercase()
		if (weaponSets.isEmpty()) weaponSets = listOf(null)

		for (weaponSet in weaponSets){
			Tasks.sync {
				fireHeavyWeapons(shootDirection, target, weaponSet = weaponSet?.name?.lowercase())
				fireLightWeapons(shootDirection, target, weaponSet = weaponSet?.name?.lowercase())
			}
		}
	}

	/** Fires light weapons (left click) in a direction */
	protected fun fireLightWeapons(direction: Vector, target: Vector? = null, weaponSet: String? = null) {
		starship.debug("Firing light weapons: Set: $weaponSet")
		AIControlUtils.shootInDirection(controller, direction, leftClick = true, target = target, weaponSet = weaponSet)
	}

	/** Fires heavy weapons (right click) in a direction */
	protected fun fireHeavyWeapons(direction: Vector, target: Vector? = null, weaponSet: String? = null) {
		starship.debug("Firing heavy weapons: Set: $weaponSet")
		AIControlUtils.shootInDirection(controller, direction, leftClick = false, target = target, weaponSet = weaponSet)
	}

	/** Updates all auto weapons,that are in range, to fire on the target */
	protected fun handleAutoWeapons(origin: Vec3i, target: AITarget) {
		val (x, y, z) = origin
		val distance = target.getVec3i(false).distance(x, y, z)
		val weaponSet = controller.getAutoSetInRange(distance)?.name?.lowercase()

		if (weaponSet == null) {
			AIControlUtils.unSetAllWeapons(controller)
			return
		}

		AIControlUtils.setAutoWeapons(controller, weaponSet, target.getAutoTurretTarget())
	}

	private fun sampleDirection(direction: Vector) : Vector{
		val z = randomDouble(cos(shotDeviation), 1.0)
		val phi = randomDouble(0.0,2*PI)
		val r = Vector((1-z*z).pow(0.5)* cos(phi),(1-z*z).pow(0.5)* sin(phi),z)
		val rotationAxis = direction.clone().crossProduct(Vector(0,0,1))
		val rotationAngle = acos(direction.dot(Vector(0,0,1)))
		r.rotateAroundNonUnitAxis(rotationAxis,rotationAngle)
		return r
	}
}
