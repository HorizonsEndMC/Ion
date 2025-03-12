package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy.HeavyLaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector
import java.time.Duration

class HeavyLaserProjectile(
    starship: ActiveStarship?,
	name: Component,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    originalTarget: Vector,
    baseAimDistance: Int,
    sound: String
) : TrackingLaserProjectile(starship, name, loc, dir, shooter, originalTarget, baseAimDistance, HeavyLaserStarshipWeaponMultiblock.damageType) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.heavyLaser ?: ConfigurationFiles.starshipBalancing().nonStarshipFired.heavyLaser
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val maxDegrees: Double = balancing.maxDegrees
	override val range: Double = balancing.range
	override val speed: Double = balancing.speed
	override val color: Color = Color.RED
	override val particleThickness: Double = balancing.particleThickness
	override val explosionPower: Float = balancing.explosionPower
	override val soundName: String = sound

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		// firing ships larger than 4000 should not slow at all
		if ((shooter.starship?.initialBlockCount ?: 0) > 4000) return

		var speedPenalty = SLOW_FACTOR
		// ships above 1400 not affected
		if (starship.initialBlockCount >= 1400) return
		// ships above 700 half affected
		if (starship.initialBlockCount >= 700) speedPenalty = SLOW_FACTOR * 0.5

		starship.userErrorAction("Direct Control speed slowed by ${(speedPenalty * 100).toInt()}%!")
		// starship was not slowed by heavy lasers recently
		//if (starship.directControlSlowExpiryFromHeavyLasers < System.currentTimeMillis()) {
			// Only start the timer based on the first hit
			starship.directControlSlowExpiryFromHeavyLasers = System.currentTimeMillis() + Duration.ofSeconds(SLOW_DURATION_SECONDS).toMillis()
		//}
		// Reduce starship speed by the slow factor
		starship.directControlSpeedModifierFromHeavyLasers *= (1 - speedPenalty)

		Tasks.syncDelay(Duration.ofSeconds(SLOW_DURATION_SECONDS).toSeconds() * 20L) {
			// reset for individual shots
			starship.directControlSpeedModifierFromHeavyLasers /= (1 - speedPenalty)
			if (ActiveStarships.isActive(starship) && starship.directControlSlowExpiryFromHeavyLasers - 100 < System.currentTimeMillis()) {
				// hard reset to normal speed (I feel that weird double-rounding bugs might be possible)
				starship.directControlSpeedModifierFromHeavyLasers = 1.0
				starship.directControlSlowExpiryFromHeavyLasers = 0L
				starship.informationAction("Direct Control speed restored")
			}
		}
	}

	companion object {
		private const val SLOW_FACTOR = 0.15
		private const val SLOW_DURATION_SECONDS = 20L
	}
}
