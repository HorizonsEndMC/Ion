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

	private val blueParticleData = Particle.DustTransition(
		Color.fromARGB(255, 173, 216, 230),
		shooter.color,
		2.0f
	)
	private var checkpoint1 = false
	private var checkpoint2 = false

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		super.moveVisually(oldLocation, newLocation, travel)
		location.world.spawnParticle(
			Particle.DUST_COLOR_TRANSITION,
			location.x,
			location.y,
			location.z,
			4,
			0.0,
			0.0,
			0.0,
			0.0,
			blueParticleData,
			true
		)
		//Check the farther outer ring first
		if (distance < 8.0 && checkpoint1 && !checkpoint2) {
			checkpoint2 = true
			val circlePoints2 = location.circlePoints(1.5, 8, direction)
			for (point in circlePoints2) point.world.spawnParticle(
				Particle.DUST_COLOR_TRANSITION,
				point.x,
				point.y,
				point.z,
				2,
				0.0,
				0.0,
				0.0,
				0.0,
				blueParticleData,
				true
			)
		}
		if (distance < 4.0 && !checkpoint1) {
			val circlePoints1 = location.circlePoints(2.0, 8, direction)
			checkpoint1 = true
			for (point in circlePoints1) point.world.spawnParticle(
				Particle.DUST_COLOR_TRANSITION,
				point.x,
				point.y,
				point.z,
				4,
				0.0,
				0.0,
				0.0,
				0.0,
				blueParticleData,
				true
			)
		}
	}
}
