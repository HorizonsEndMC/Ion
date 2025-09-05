package net.horizonsend.ion.server.features.starship.subsystem.weapon

import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import java.util.function.Supplier

abstract class BalancedWeaponSubsystem<T: StarshipWeaponBalancing<*>>(
	starship: ActiveStarship,
	pos: Vec3i,
	val balancingSupplier: Supplier<T>
) : FiredSubsystem(starship, pos) {

	/** Balancing values for this subsystem, and projectile **/
	val balancing get() = balancingSupplier.get()

	/** Cooldown between firing shots of this weapon **/
	open val fireCooldownNanos: Long get() = balancing.fireCooldownNanos

	/** The power consumption per shot (from the weapon capacitor) **/
	open val firePowerConsumption: Int get() = balancing.firePowerConsumption

	fun isCooledDown(): Boolean {
		return System.nanoTime() - lastFire >= fireCooldownNanos
	}

	override fun getMaxPerShot(): Int? = balancing.maxPerShot

	fun canCreateSubsystem(): Boolean {
		if (starship.type.eventShip) return true
		if (!balancing.fireRestrictions.canFire && !starship.type.eventShip) return false
		return starship.initialBlockCount in balancing.fireRestrictions.minBlockCount..balancing.fireRestrictions.maxBlockCount
	}

	open fun isForwardOnly(): Boolean = balancing.isForwardOnly

	abstract fun getName(): Component
}
