package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.AssaultTurretBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.circlePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class AssaultTurretProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager,
	override val balancing: AssaultTurretBalancing.AssaultTurretProjectileBalancing
): LaserProjectile<AssaultTurretBalancing.AssaultTurretProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {

	private var checkpoint1 = false
	private var checkpoint2 = false


	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val particle = Particle.DUST
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 4f)
		super.moveVisually(oldLocation, newLocation, travel)

		//Checks the smaller ring that's farther out first
		if (distance < 8.0 && !checkpoint2 && checkpoint1) {
			checkpoint2 = true
			val circlePoints2 = location.circlePoints(1.5, 8, direction)
			for (point in circlePoints2) point.world.spawnParticle(
				particle,
				point.x,
				point.y,
				point.z,
				2,
				0.0,
				0.0,
				0.0,
				0.0,
				dustOptions,
				true
			)
		}

		if (distance < 4.0 && !checkpoint1) {
			checkpoint1 = true
			val circlePoints1 = location.circlePoints(2.0, 8, direction)
			for (point in circlePoints1) point.world.spawnParticle(
				particle,
				point.x,
				point.y,
				point.z,
				4,
				0.0,
				0.0,
				0.0,
				0.0,
				dustOptions,
				true
			)
		}
	}
}
