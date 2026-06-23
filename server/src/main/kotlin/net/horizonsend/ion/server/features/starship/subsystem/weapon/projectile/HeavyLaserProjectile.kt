package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.configuration.starship.HeavyLaserBalancing.HeavyLaserProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.HeavyLaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffect
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class HeavyLaserProjectile(
    source: ProjectileSource,
	name: Component,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    originalTarget: Vector,
    baseAimDistance: Int
) : TrackingLaserProjectile<HeavyLaserProjectileBalancing>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, HeavyLaserStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.RED

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		// firing ships larger than 4000 should not slow at all
		if ((shooter.starship?.initialBlockCount ?: 0) > 4000) return

		var speedPenalty = balancing.effectStrength
		// ships above 3500 not affected
		if (starship.initialBlockCount >= 3500 || starship.type.tech2) return
		// ships above 2500 half affected
		if (starship.initialBlockCount >= 2500) speedPenalty = balancing.effectStrength * 0.5

		starship.addStatusEffect(StarshipStatusEffect(
			StarshipStatusEffectTypes.DIRECT_CONTROL_SLOW,
			speedPenalty,
			balancing.effectDurationMillis,
			shooter.starship
		))

		starship.userErrorAction("Direct Control speed slowed by ${(speedPenalty * 100).toInt()}%!")
	}
}
