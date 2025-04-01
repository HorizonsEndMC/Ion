package net.horizonsend.ion.server.features.starship.subsystem.weapon

import net.horizonsend.ion.server.configuration.StarshipWeapons.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.util.Vector
import java.util.function.Supplier

abstract class WeaponSubsystem<T: StarshipWeaponBalancing<*>>(
	starship: ActiveStarship,
	pos: Vec3i,
	val balancingSupplier: Supplier<T>
) : StarshipSubsystem(starship, pos) {
	val name = this.javaClass.simpleName.removeSuffix("WeaponSubsystem")
	var lastFire: Long = System.nanoTime()

	/** Balancing values for this subsystem, and projectile **/
	val balancing get() = balancingSupplier.get()

	/** Cooldown between firing shots of this weapon **/
	open val fireCooldownNanos: Long get() = balancing.fireCooldownNanos

	/** The power consumption per shot (from the weapon capacitor) **/
	open val firePowerConsumption: Int get() = balancing.firePowerConsumption

	fun isCooledDown(): Boolean {
		return System.nanoTime() - lastFire >= fireCooldownNanos
	}

	open fun getMaxPerShot(): Int? = balancing.maxPerShot

	abstract fun getAdjustedDir(dir: Vector, target: Vector): Vector

	/**
	 * Check if the weapon is obstructed etc.
	 * @return True if it should fire, false if it shouldn't
	 */
	abstract fun canFire(dir: Vector, target: Vector): Boolean

	fun canCreateSubsystem(): Boolean {
		if (starship.type.eventShip) return true
		if (!balancing.fireRestrictions.canFire && !starship.type.eventShip) return false
		return starship.initialBlockCount in balancing.fireRestrictions.minBlockCount..balancing.fireRestrictions.maxBlockCount
	}

	open fun isForwardOnly(): Boolean = balancing.isForwardOnly

	fun postFire() {
		lastFire = System.nanoTime()
	}

	abstract fun getName(): Component
}
