package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.TestBoidCannonBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class TestBoidProjectile(
    source: ProjectileSource,
    name: Component,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    otherBoids: MutableList<BoidProjectile<*>>,
    damageType: DamageType
) : BoidParticleProjectile<TestBoidCannonBalancing.TestBoidCannonProjectileBalancing>(source, name, loc, dir, shooter, otherBoids, damageType) {
    override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
        val particle = Particle.DUST
        val dustOptions = Particle.DustOptions(Color.AQUA, 5f)

        location.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
    }
}