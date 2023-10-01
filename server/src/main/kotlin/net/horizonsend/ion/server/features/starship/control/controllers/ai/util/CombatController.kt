package net.horizonsend.ion.server.features.starship.control.controllers.ai.util

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIStarshipTemplates
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

interface CombatController : LocationObjectiveAI {
	val starship: ActiveStarship
	var target: ActiveStarship?

	// Weapon sets
	val manualWeaponSets: MutableList<AIStarshipTemplates.WeaponSet>
	val autoWeaponSets: MutableList<AIStarshipTemplates.WeaponSet>

	// Shield Health indicators
	val shields get() = starship.shields
	val shieldCount get() = shields.size
	val averageHealth get() = shields.sumOf { it.powerRatio } / shieldCount.toDouble()


	/** The location that should be navigated towards */
	var locationObjective: Location?

	/** Gets the location of the targeted ship */
	fun getTargetLocation(): Location?

	/**
	 * Use Vec3i as a target to allow block targeting
	 *
	 * Lambda allows modification of the aiming direction
	 **/
	fun fireAllWeapons(origin: Vec3i, target: Vec3i, faceDirection: BlockFace? = null, directionMod: (Vector) -> Unit) {
		if (this !is AIController) return

		val (x, y, z) = origin
		val distance = target.distance(x, y, z)

		val weaponSet = autoWeaponSets.shuffled().firstOrNull { it.engagementRange.containsDouble(distance) }?.name
		val direction = getDirection(Vec3i(getCenter()), target).normalize()

		directionMod(direction)

		Tasks.sync {
			faceDirection?.let { AIControlUtils.faceDirection(this, faceDirection) }

			fireHeavyWeapons(direction, target.toVector(), node = weaponSet)
			fireLightWeapons(direction, target.toVector(), weaponSet = weaponSet)
		}
	}

	/** Fires light weapons (left click) in a direction */
	fun fireLightWeapons(direction: Vector, target: Vector? = null, weaponSet: String? = null) {
		if (this !is AIController) return

		AIControlUtils.shootInDirection(this, direction, leftClick = true, target = target, weaponSet = weaponSet)
	}

	/** Fires heavy weapons (right click) in a direction */
	fun fireHeavyWeapons(direction: Vector, target: Vector? = null, node: String? = null) {
		if (this !is AIController) return

		AIControlUtils.shootInDirection(this, direction, leftClick = false, target = target, weaponSet = node)
	}

	/** Updates all auto weapons,that are in range, to fire on the target */
	fun handleAutoWeapons(origin: Vec3i) {
		val target = this.target ?: return
		if (this !is AIController) return

		val (x, y, z) = origin
		val distance = target.centerOfMass.distance(x, y, z)
		val weaponSet = autoWeaponSets.shuffled().firstOrNull { it.engagementRange.containsDouble(distance) }?.name

		if (weaponSet == null) {
			AIControlUtils.unSetAllWeapons(this)
			return
		}

		AIControlUtils.setAutoWeapons(this, weaponSet, target)
	}
}
