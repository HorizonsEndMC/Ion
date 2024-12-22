package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipWeapons
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
) : TrackingLaserProjectile(starship, name, loc, dir, shooter, originalTarget, baseAimDistance) {
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
		var speedPenalty = 0.15
		if (starship.initialBlockCount >= 1400) return
		if (starship.initialBlockCount >= 700) speedPenalty = 0.08
		starship.userErrorAction("Direct Control speed slowed by ${"%.0f".format(speedPenalty*100)}%!")
		starship.directControlSpeedModifier *= 1 - speedPenalty
		starship.lastDirectControlSpeedSlowed = System.currentTimeMillis() + Duration.ofSeconds(7).toMillis()

		Tasks.syncDelay(Duration.ofSeconds(20).toSeconds() * 20L) {
			// reset for individual shots
			starship.directControlSpeedModifier /= 1 - speedPenalty
			if (ActiveStarships.isActive(starship) && starship.lastDirectControlSpeedSlowed - 4000 < System.currentTimeMillis()) {
				// hard reset to normal speed (I feel that weird double-rounding bugs might be possible)
				starship.directControlSpeedModifier = 1.0
				starship.informationAction("Direct Control speed restored")
			}
		}
	}
}
