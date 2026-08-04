package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.AutocannonBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.AutocannonMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class AutocannonProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager,
	private val shotIndex: Int,
	private val multiblock: AutocannonMultiblock
) : LaserProjectile<AutocannonBalancing.AutocannonProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {
    override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
        super.moveVisually(oldLocation, newLocation, travel)

        if (System.nanoTime() - this.firedAtNanos > shotIndex * TimeUnit.MILLISECONDS.toNanos(balancing.delayMillis.toLong())) {
            this.speed = balancing.speed
        }
    }
	override var speed = balancing.speed
    override fun fire() {
        super.fire()

        this.speed = 1.0
    }
}
