package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.configuration.starship.NeutralizerBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.NeutralizerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class NeutralizerProjectile(
    source: ProjectileSource,
	name: Component,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    originalTarget: Vector,
    baseAimDistance: Int
) : TrackingLaserProjectile<NeutralizerBalancing.NeutralizerProjectileBalancing>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, NeutralizerStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.ORANGE

    override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
        val shooterStarship = shooter.starship ?: return
		starship.shieldRegenModifier = 0.8
		starship.userErrorAction("Ship shield regeneration disrupted!")
		val endLocation = shooterStarship.centerOfMass.toLocation(shooterStarship.world)
        shooterStarship.world.spawnParticle(
            Particle.TRAIL,
            impactLocation,
            10,
            0.0,
            0.0,
            0.0,
            0.0,
            Particle.Trail(endLocation,Color.YELLOW,80)
        )
    }
}
