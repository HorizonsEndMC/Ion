package net.horizonsend.ion.server.features.starship.subsystem.weapon

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.util.Vector

abstract class FiredSubsystem(
	starship: ActiveStarship,
	pos: Vec3i
) : StarshipSubsystem(starship, pos) {
	val name = this.javaClass.simpleName.removeSuffix("WeaponSubsystem")

	var lastFire: Long = System.nanoTime()

	abstract fun getMaxPerShot(): Int?

	abstract fun getAdjustedDir(dir: Vector, target: Vector): Vector

	/**
	 * Check if the weapon is obstructed etc.
	 * @return True if it should fire, false if it shouldn't
	 */
	abstract fun canFire(dir: Vector, target: Vector): Boolean

	fun postFire() {
		lastFire = System.nanoTime()
	}
}
