package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector

abstract class LaserProjectile(
    starship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    shooter: Player?
) : ParticleProjectile(starship, loc, dir, shooter) {
    abstract val color: Color
    abstract val particleThickness: Double

    override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
        val particle = Particle.REDSTONE
        val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 4f)
        loc.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
    }
}
