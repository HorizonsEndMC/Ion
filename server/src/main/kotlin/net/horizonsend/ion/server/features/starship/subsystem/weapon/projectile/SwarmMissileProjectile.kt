package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.SwarmMissileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.lerp
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class SwarmMissileProjectile(
    source: ProjectileSource,
    name: Component,
    loc: Location,
    val dir: Vector,
    val initialDir: Vector,
    shooter: Damager,
    otherBoids: MutableList<BoidProjectile<*>>,
    damageType: DamageType
) : BoidParticleProjectile<SwarmMissileBalancing.SwarmMissileProjectileBalancing>(source, name, loc, dir, shooter, otherBoids, damageType) {
    var flightPath1Completed = false
    var flightPath2Completed = false

    override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
        val particle = Particle.DUST
        val dustOptions = Particle.DustOptions(Color.AQUA, 5f)

        location.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
    }

    override fun tick() {
        if (!flightPath1Completed) {
            direction = initialDir.clone().multiply(0.25) // make the projectile launch parallel from the launcher, and slower

            if (distance > 10) {
                distance = 0.0
                flightPath1Completed = true
            }

            super.tick()
            return
        } else if (!flightPath2Completed) {
            direction = direction.clone().lerp(calculateBoidDirection(direction), 0.2)

            if (direction.angle(dir) < Math.PI / 6) {
                flightPath2Completed = true
            }

            super.tick()
            return
        }

        direction = calculateBoidDirection(direction)

        super.tick()
    }
}