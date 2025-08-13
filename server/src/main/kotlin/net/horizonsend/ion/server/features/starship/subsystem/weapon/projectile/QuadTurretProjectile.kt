package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.QuadTurretBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class QuadTurretProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val speed: Double,
	override val color: Color,
	shooter: Damager
): LaserProjectile<QuadTurretBalancing.QuadTurretProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {

		val particle1 = Particle.GUST
		val particle2 = Particle.DUST
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 3f)
		location.world.spawnParticle(particle1, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, null, force)
		location.world.spawnParticle(particle2, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
	}
}
