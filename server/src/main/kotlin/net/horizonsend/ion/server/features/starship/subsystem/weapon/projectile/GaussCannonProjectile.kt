package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.GaussCannonBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class GaussCannonProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager,
	override val balancing: GaussCannonBalancing.GaussCannonProjectileBalancing
): LaserProjectile<GaussCannonBalancing.GaussCannonProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {

		val particle1 = Particle.DUST
		val particle2 = Particle.SOUL_FIRE_FLAME
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 3f)
		location.world.spawnParticle(particle1, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
		val potentialCirclePoints = location.alongVector(direction.normalize().multiply(500), 50)
		for (point in potentialCirclePoints) {
			val circlePoints = point.circlePoints(2.0, 8, direction)
			for (circlePoint in circlePoints) {

				location.world.spawnParticle(particle2, circlePoint.x, circlePoint.y, circlePoint.z, 1, 0.0, 0.0, 0.0, 0.0, null, force)
			}
		}
	}
}
