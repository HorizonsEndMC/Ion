package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.StarshipBoidProjectileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

abstract class BoidParticleProjectile<T: StarshipBoidProjectileBalancing>(
    source: ProjectileSource,
    name: Component,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    target: Vector,
    otherBoids: MutableList<BoidProjectile<*>>,
    damageType: DamageType
) : BoidProjectile<T>(source, name, loc, dir, target, shooter, otherBoids, damageType) {
    override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
        for (lineLoc in oldLocation.alongVector(newLocation.toVector().subtract(oldLocation.toVector()), 10)) {
            spawnParticle(lineLoc.x, lineLoc.y, lineLoc.z, true)
        }
    }

    protected abstract fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean)
}