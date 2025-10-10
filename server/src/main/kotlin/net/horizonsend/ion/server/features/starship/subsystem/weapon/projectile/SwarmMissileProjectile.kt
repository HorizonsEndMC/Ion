package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.SwarmMissileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class SwarmMissileProjectile(
    source: ProjectileSource,
    name: Component,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    otherBoids: MutableList<BoidProjectile<*>>,
    damageType: DamageType
) : BoidProjectile<SwarmMissileBalancing.SwarmMissileProjectileBalancing>(source, name, loc, dir, shooter, otherBoids, damageType) {
    override fun moveVisually(
        oldLocation: Location,
        newLocation: Location,
        travel: Double
    ) {
        TODO("Not yet implemented")
    }
}