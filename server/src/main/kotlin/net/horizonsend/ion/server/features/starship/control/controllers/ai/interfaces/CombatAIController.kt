package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

interface CombatAIController : VariableObjectiveController {
	val starship: ActiveStarship
	var target: AITarget?

	// Weapon sets
	val manualWeaponSets: Set<WeaponSet>
	val autoWeaponSets: Set<WeaponSet>

	var shouldFaceTarget: Boolean
	var turnTicks: Int
	var turnCooldown: Int

	// Shield Health indicators
	val shields get() = starship.shields
	val shieldCount get() = shields.size
	val averageHealth get() = shields.sumOf { it.powerRatio } / shieldCount.toDouble()

	/** The location that should be navigated towards */
	var locationObjective: Location?

	override fun getObjective(): Vec3i? = locationObjective?.let { Vec3i(it) }

	/**
	 * Use Vec3i as a target to allow block targeting
	 *
	 * Lambda allows modification of the aiming direction
	 **/
	fun fireAllWeapons(origin: Vec3i, target: Vec3i, directionMod: (Vector) -> Unit) {
		if (this !is AIController) return

		val (x, y, z) = origin
		val distance = target.distance(x, y, z)

		val weaponSet = manualWeaponSets.firstOrNull { it.engagementRange.containsDouble(distance) }?.name?.lowercase()
		val direction = getDirection(Vec3i(getCenter()), target).normalize()

		directionMod(direction)

		Tasks.sync {
			fireHeavyWeapons(direction, target.toVector(), weaponSet = weaponSet)
			fireLightWeapons(direction, target.toVector(), weaponSet = weaponSet)
		}
	}

	fun handleRotation(faceDirection: BlockFace) {
		if (this !is AIController) return
		if (!shouldFaceTarget) return

		if (turnTicks >= turnCooldown) {
			turnTicks = 0
			return
		}

		turnTicks++

		AIControlUtils.faceDirection(this, faceDirection)
	}

	/** Fires light weapons (left click) in a direction */
	fun fireLightWeapons(direction: Vector, target: Vector? = null, weaponSet: String? = null) {
		if (this !is AIController) return

		debugAudience.debug("Firing light weapons: Set: $weaponSet")
		AIControlUtils.shootInDirection(this, direction, leftClick = true, target = target, weaponSet = weaponSet)
	}

	/** Fires heavy weapons (right click) in a direction */
	fun fireHeavyWeapons(direction: Vector, target: Vector? = null, weaponSet: String? = null) {
		if (this !is AIController) return

		debugAudience.debug("Firing heavy weapons: Set: $weaponSet")
		AIControlUtils.shootInDirection(this, direction, leftClick = false, target = target, weaponSet = weaponSet)
	}

	/** Updates all auto weapons,that are in range, to fire on the target */
	fun handleAutoWeapons(origin: Vec3i) {
		val target = this.target ?: return
		if (this !is AIController) return

		val (x, y, z) = origin
		val distance = target.getVec3i(true).distance(x, y, z)
		val weaponSet = autoWeaponSets.firstOrNull { it.engagementRange.containsDouble(distance) }?.name?.lowercase()

		if (weaponSet == null) {
			AIControlUtils.unSetAllWeapons(this)
			return
		}

		AIControlUtils.setAutoWeapons(this, weaponSet, target.getAutoTurretTarget())
	}
}
