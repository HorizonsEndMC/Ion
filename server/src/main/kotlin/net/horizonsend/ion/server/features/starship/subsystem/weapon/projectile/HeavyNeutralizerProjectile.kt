package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.configuration.starship.HeavyNeutralizerBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.HeavyNeutralizerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class HeavyNeutralizerProjectile(
    source: ProjectileSource,
	name: Component,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    originalTarget: Vector,
    baseAimDistance: Int
) : TrackingLaserProjectile<HeavyNeutralizerBalancing.HeavyNeutralizerProjectileBalancing>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, HeavyNeutralizerStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.ORANGE

    override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
        val shooterStarship = shooter.starship ?: return
		starship.shieldRegenModifier = 0.2
		starship.cruiseData.accel *= 0.7
		starship.userErrorAction("Ship shield regeneration and acceleration severely disrupted!")
		val endLocation = shooterStarship.centerOfMass.toLocation(shooterStarship.world)
        shooterStarship.world.spawnParticle(
            Particle.TRAIL,
            impactLocation,
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            Particle.Trail(endLocation,Color.YELLOW,60)
        )
    }
}
