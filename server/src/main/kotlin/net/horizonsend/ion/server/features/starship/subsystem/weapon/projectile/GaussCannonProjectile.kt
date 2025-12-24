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
import kotlin.math.roundToInt

class GaussCannonProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager,
	override val balancing: GaussCannonBalancing.GaussCannonProjectileBalancing
): LaserProjectile<GaussCannonBalancing.GaussCannonProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val particle1 = Particle.DUST
		val particle2 = Particle.SOUL_FIRE_FLAME
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 3f)
		location.world.spawnParticle(
			particle1,
			location.x,
			location.y,
			location.z,
			4,
			0.0,
			0.0,
			0.0,
			0.0,
			dustOptions,
			true
		)
		if (distance.toInt() % 10 == 0) {
			val circlePoints = location.circlePoints(2.0, 8, direction)
			for (point in circlePoints) point.world.spawnParticle(
				particle2,
				point.x,
				point.y,
				point.z,
				2,
				0.0,
				0.0,
				0.0,
				0.0,
				null,
				true
			)
		}
	}
}
